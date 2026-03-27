package minigame.LuckyNumber;

import consts.ConstMiniGame;
import minigame.cost.LuckyNumberCost;
import npc.Npc;
import player.Player;
import utils.Util;

import java.util.List;
import java.util.stream.Collectors;

public class LuckyNumberHandler {

    // ================= RESULT =================

    public static String showOneResult() {
        return !LuckyNumber.DATA_RESULT.isEmpty()
                ? String.format("%02d", LuckyNumber.DATA_RESULT.get(LuckyNumber.DATA_RESULT.size() - 1))
                : "";
    }

    public static String showTenResult() {
        List<Integer> data = LuckyNumber.DATA_RESULT;

        if (data != null && !data.isEmpty()) {
            int start = Math.max(0, data.size() - 10);

            return data.subList(start, data.size()).stream()
                    .map(i -> String.format("%02d", i))
                    .collect(Collectors.joining(","));
        }
        return "";
    }

    public static String showTenPlayResult() {
        List<String> data = LuckyNumber.DATA_PLAYER_RESULT;

        if (data != null && !data.isEmpty()) {
            int start = Math.max(0, data.size() - 10);

            return data.subList(start, data.size())
                    .stream()
                    .collect(Collectors.joining(","));
        }
        return "";
    }

    // ================= MENU =================

    public static void showMenuCSMM(Npc npc, Player player, int type) {

        String ketQua = showOneResult();
        String listKetQua = showTenResult();
        String listPlayer = showTenPlayResult();
        String resultPlayerSelect = LuckyNumberService.strNumber((int) player.id, true);

        StringBuilder npcSay = new StringBuilder();

        if (!ketQua.isEmpty()) {
            npcSay.append("Kết quả giải trước: ").append(ketQua).append("\n");
        }

        if (!listKetQua.isEmpty()) {
            npcSay.append(listKetQua).append("\n");
        }

        if (!listPlayer.isEmpty()) {
            npcSay.append("Thắng giải trước: ").append(listPlayer).append("\n");
        }

        npcSay.append("Tổng giải thưởng: ")
                .append(Util.numberFormatTanTai(getTotalReward(type)))
                .append(" ")
                .append(getMoneyName(type))
                .append("\n<")
                .append(LuckyNumberCost.timeGame)
                .append("> giây");

        if (!resultPlayerSelect.isEmpty()) {
            npcSay.append("\nCác số bạn chọn: ").append(resultPlayerSelect);
        }

        npc.createOtherMenu(player,
                getMenuId(type),
                npcSay.toString(),
                "Cập nhật",
                getOption1(type),
                getOption2(type),
                getOption3(type),
                "Hướng\ndẫn\nthêm",
                "Đóng");
    }

    // ================= HELPER =================

    private static int getMenuId(int type) {
        return switch (type) {
            case LuckyNumberCost.costGold -> ConstMiniGame.MENU_PLAY_LUCKY_NUMBER_GOLD;
            case LuckyNumberCost.costGem -> ConstMiniGame.MENU_PLAY_LUCKY_NUMBER_GEM;
            default -> -1;
        };
    }

    private static long getTotalReward(int type) {
        return switch (type) {
            case LuckyNumberCost.costGold -> LuckyNumberCost.costGold;
            case LuckyNumberCost.costGem -> LuckyNumberCost.costGem;
            default -> 0;
        };
    }

    private static String getMoneyName(int type) {
        return switch (type) {
            case LuckyNumberCost.costGold -> "vàng";
            case LuckyNumberCost.costGem -> "ngọc";
            default -> "";
        };
    }

    private static String getOption1(int type) {
        return switch (type) {
            case LuckyNumberCost.costGold -> "1 Số\n1 Tr vàng";
            case LuckyNumberCost.costGem -> "1 Số\n5 ngọc xanh";
            default -> "";
        };
    }

    private static String getOption2(int type) {
        return switch (type) {
            case LuckyNumberCost.costGold -> "Ngẫu nhiên\n1 số lẻ\n1 Tr vàng";
            case LuckyNumberCost.costGem -> "Ngẫu nhiên\n1 số lẻ\n5 ngọc xanh";
            default -> "";
        };
    }

    private static String getOption3(int type) {
        return switch (type) {
            case LuckyNumberCost.costGold -> "Ngẫu nhiên\n1 số chẵn\n1 Tr vàng";
            case LuckyNumberCost.costGem -> "Ngẫu nhiên\n1 số chẵn\n5 ngọc xanh";
            default -> "";
        };
    }
}