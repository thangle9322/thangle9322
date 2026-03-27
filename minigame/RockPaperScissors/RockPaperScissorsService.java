package minigame.RockPaperScissors;

import consts.ConstFont;
import consts.ConstMiniGame;
import minigame.RockPaperScissors.RockPaperScissors;
import npc.Npc;
import player.Player;
import services.Service;
import utils.Util;

public class RockPaperScissorsService {

    // ================== MAIN ==================
    public static void play(Npc npc, Player player, int playerPick) {

        if (!canPlay(player)) {
            Service.gI().sendThongBao(player, "Không đủ vàng để chơi!");
            return;
        }

        int serverPick = Util.nextInt(0, 2);

        player.idMark.setKeoBuaBaoPlayer((byte) playerPick);
        player.idMark.setKeoBuaBaoServer((byte) serverPick);

        // 🎬 Player ra chiêu trước
        Service.gI().sendEffAllPlayer(player, 1000 + playerPick, 1, 2, 1);

        // 🎬 NPC ra sau (fake qua player)
        Util.delay(300, () -> {
            Service.gI().sendEffAllPlayer(player, 1000 + serverPick, 1, 2, 1);
        });

        int result = checkWinLose(playerPick, serverPick);

        // 🎯 Delay rồi mới show kết quả (cho mượt)
        Util.delay(600, () -> {
            showResult(npc, player, result);

            // 🎉 hiệu ứng kết quả
            if (result == 1) {
                Service.gI().sendEffAllPlayer(player, 1100, 1, 1, 1); // win
            } else if (result == 2) {
                Service.gI().sendEffAllPlayer(player, 1200, 1, 1, 1); // lose (khác id)
            }
        });
    }

    // ================== SHOW RESULT ==================
    public static void showResult(Npc npc, Player player, int result) {

        int p = player.idMark.getKeoBuaBaoPlayer();
        int s = player.idMark.getKeoBuaBaoServer();
        long moneyBet = player.idMark.getMoneyKeoBuaBao();

        String ketQuaPlayer = convertNumberToString(p);
        String ketQuaServer = convertNumberToString(s);
        String money = Util.numberFormatTanTai(moneyBet);

        String text;

        switch (result) {
            case 1 -> {
                text = ConstFont.BOLD_GREEN
                        + "[CHIẾN THẮNG]\n"
                        + "Bạn ra: <" + ketQuaPlayer + ">\n"
                        + "Đối thủ ra: <" + ketQuaServer + ">\n"
                        + "Phần thưởng: +" + money + " vàng";

                player.inventory.gold += moneyBet;
            }
            case 2 -> {
                text = ConstFont.BOLD_RED
                        + "[THẤT BẠI]\n"
                        + "Bạn ra: <" + ketQuaPlayer + ">\n"
                        + "Đối thủ ra: <" + ketQuaServer + ">\n"
                        + "Mất: -" + money + " vàng";

                player.inventory.gold -= moneyBet;
            }
            default -> {
                text = ConstFont.BOLD_BLUE
                        + "[HÒA]\n"
                        + "Bạn ra: <" + ketQuaPlayer + ">\n"
                        + "Đối thủ ra: <" + ketQuaServer + ">\n"
                        + "Không thay đổi phần thưởng";
            }
        }

        npc.createOtherMenu(player, ConstMiniGame.MENU_PLAY_KEO_BUA_BAO,
                text,
                "Kéo", "Búa", "Bao", "Đổi\nmức cược", "Nghỉ chơi");

        Service.gI().sendMoney(player);
    }

    // ================== CHECK ==================
    public static boolean canPlay(Player player) {
        return player.inventory.gold >= player.idMark.getMoneyKeoBuaBao();
    }

    public static int checkWinLose(int player, int server) {
        if (player == server) return 3;

        switch (player) {
            case RockPaperScissors.KEO:
                return (server == RockPaperScissors.BUA) ? 2 : 1;
            case RockPaperScissors.BUA:
                return (server == RockPaperScissors.BAO) ? 2 : 1;
            case RockPaperScissors.BAO:
                return (server == RockPaperScissors.KEO) ? 2 : 1;
        }
        return 2;
    }

    // ================== UTIL ==================
    public static String convertNumberToString(int i) {
        return switch (i) {
            case 0 -> "Kéo";
            case 1 -> "Búa";
            case 2 -> "Bao";
            default -> "";
        };
    }
}