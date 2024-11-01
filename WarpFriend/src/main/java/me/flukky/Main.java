package me.flukky;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends JavaPlugin implements CommandExecutor, TabCompleter {

    private final Map<Player, Player> warpRequests = new HashMap<>();

    @Override
    public void onEnable() {
        this.getCommand("warpfriend").setExecutor(this);
        this.getCommand("wa").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("warpfriend")) {
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "เพื่อนของคุณไม่ได้ออนไลน์!");
                return true;
            }

            Player friend = Bukkit.getPlayer(args[0]);
            String paymentType = args[1].toLowerCase();

            if (friend == null || !friend.isOnline()) {
                player.sendMessage(ChatColor.RED + "Your friend is not online.");
                return true;
            }

            boolean hasEnoughLevels = player.getLevel() >= 10;
            boolean hasEnoughDiamonds = player.getInventory().contains(Material.DIAMOND, 15);

            boolean hasEnoughLevelsFriend = friend.getLevel() >= 10;
            boolean hasEnoughDiamondsFriend = friend.getInventory().contains(Material.DIAMOND, 15);

            if (paymentType.equals("exp") && !hasEnoughLevels) {
                player.sendMessage(ChatColor.RED + "คุณไม่มี exp เพียงพอ (ต้องการ 10 levels)");
                return true;
            }
            if (paymentType.equals("exp") && !hasEnoughLevelsFriend) {
                player.sendMessage(ChatColor.RED + "เพื่อนของคุณไม่มี exp เพียงพอ (ต้องการ 10 levels)");
                return true;
            }
            if (paymentType.equals("diamond") && !hasEnoughDiamonds) {
                player.sendMessage(ChatColor.RED + "คุณไม่มี diamond เพียงพอ (ต้องการ 15 diamonds)");
                return true;
            }
            if (paymentType.equals("diamond") && !hasEnoughDiamondsFriend) {
                player.sendMessage(ChatColor.RED + "เพื่อนของคุณไม่มี diamond เพียงพอ (ต้องการ 15 diamonds)");
                return true;
            }

            warpRequests.put(friend, player);
            player.sendMessage(ChatColor.GREEN + "ส่งคำขอวาร์ปไปที่ " + friend.getName() + " โปรดรอให้พวกเขายอมรับ.");
            friend.sendMessage(ChatColor.BLUE + player.getName() + " ต้องการเทเลพอร์ตไปหาคุณโดยใช้ " + paymentType
                    + " พิมพ์ /wa เพื่อยอมรับ");
            return true;

        } else if (command.getName().equalsIgnoreCase("wa")) {
            if (!warpRequests.containsKey(player)) {
                player.sendMessage(ChatColor.RED + "You have no pending warp requests.");
                return true;
            }

            Player requester = warpRequests.get(player);
            String paymentType = ""; // เปลี่ยนตามเงื่อนไขที่ถูกต้อง

            boolean hasEnoughLevelsRequester = requester.getLevel() >= 10;
            boolean hasEnoughDiamondsRequester = requester.getInventory().contains(Material.DIAMOND, 15);

            boolean hasEnoughLevelsPlayer = player.getLevel() >= 10;
            boolean hasEnoughDiamondsPlayer = player.getInventory().contains(Material.DIAMOND, 15);

            // ตรวจสอบประเภทการชำระเงินจากคำขอที่เลือกไว้
            if (warpRequests.containsKey(player)) {
                // ตรวจสอบจากคำขอที่ถูกส่งเข้ามา ถ้าเลือก exp ให้ทำการเช็ค exp
                if (hasEnoughLevelsRequester && hasEnoughLevelsPlayer) {
                    paymentType = "exp"; // ใช้ exp เป็นการชำระเงิน
                } else if (hasEnoughDiamondsRequester && hasEnoughDiamondsPlayer) {
                    paymentType = "diamond"; // ใช้ diamond เป็นการชำระเงิน
                } else {
                    if (!hasEnoughLevelsRequester) {
                        player.sendMessage(ChatColor.RED + "คุณไม่มี diamond เพียงพอสำหรับการวาร์ป");
                        return false;
                    }
                    if (!hasEnoughLevelsPlayer) {
                        requester.sendMessage(ChatColor.RED + player.getName() + " ไม่มี diamond เพียงพอสำหรับการวาร์ป");
                        return false;
                    }

                    if (!hasEnoughDiamondsRequester) {
                        player.sendMessage(ChatColor.RED + "คุณไม่มี diamond เพียงพอสำหรับการวาร์ป");
                        return false;
                    }
                    if (!hasEnoughDiamondsPlayer) {
                        requester.sendMessage(ChatColor.RED + player.getName() + " ไม่มี diamond เพียงพอสำหรับการวาร์ป");
                        return false;
                    }
                }
            }

            // ดำเนินการตามประเภทการชำระเงิน
            if (paymentType.equals("exp")) {
                requester.setLevel(requester.getLevel() - 10);
                player.setLevel(player.getLevel() - 10);
            } else if (paymentType.equals("diamond")) {
                requester.getInventory().removeItem(new ItemStack(Material.DIAMOND, 15));
                player.getInventory().removeItem(new ItemStack(Material.DIAMOND, 15));
            } else {
                // ตรวจสอบกรณีที่ไม่มีเพียงพอ
                if (!hasEnoughDiamondsRequester) {
                    requester.sendMessage(ChatColor.RED + "คุณมี diamond ไม่เพียงพอสำหรับการวาร์ป");
                    player.sendMessage(ChatColor.RED + requester.getName() + " มี diamond ไม่เพียงพอสำหรับการวาร์ป");
                    return false;
                }

                if (!hasEnoughDiamondsPlayer) {
                    player.sendMessage(ChatColor.RED + "คุณมี diamond ไม่เพียงพอสำหรับการวาร์ป");
                    return false;
                }
            }

            // เพิ่ม delay 3 วินาที (3000 มิลลิวินาที) ก่อนวาร์ป พร้อมกับนับถอยหลัง
            final String finalPaymentType = paymentType; // ใช้ตัวแปร final
            new BukkitRunnable() {
                int countdown = 3;

                @Override
                public void run() {
                    if (countdown > 0) {
                        requester.sendMessage(ChatColor.YELLOW + "Teleporting in " + countdown + "...");
                        player.sendMessage(ChatColor.YELLOW + requester.getName() + " is teleporting in " + countdown + "...");
                        countdown--;
                    } else {
                        requester.teleport(player.getLocation());
                        player.sendMessage(ChatColor.GREEN + "คุณได้ยอมรับคำขอเทเลพอร์ตจาก " + requester.getName()
                                + " โดยใช้ " + finalPaymentType);
                        requester.sendMessage(ChatColor.BLUE + player.getName() + " ได้ยอมรับคำขอเทเลพอร์ตของคุณโดยใช้ "
                                + finalPaymentType);
                        cancel(); // หยุดการนับถอยหลัง
                    }
                }
            }.runTaskTimer(this, 0, 20); // 20 ticks = 1 second

            // ลบคำขอวาร์ป
            warpRequests.remove(player);
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // เติมชื่อผู้เล่นที่ออนไลน์
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
        } else if (args.length == 2) {
            // เติมประเภทการชำระเงิน
            completions.add("exp");
            completions.add("diamond");
        }

        return completions;
    }
}
