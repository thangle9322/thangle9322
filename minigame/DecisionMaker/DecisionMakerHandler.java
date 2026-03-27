package minigame.DecisionMaker;

import consts.ConstMiniGame;
import minigame.cost.DecisionMakerCost;
import npc.Npc;
import player.Player;
import services.Service;
import utils.Util;

public class DecisionMakerHandler {

    // ================= MENU =================
    public static void showMenuSelect(Npc npc, Player player, int type) {
        long totalNormal = DecisionMakerService.getTotalMoney(type, true);
        long totalVIP = DecisionMakerService.getTotalMoney(type, false);

        npc.createOtherMenu(player, getMenuId(type),
                "Tổng giải thưởng: " + Util.numberToMoney(totalNormal) + " " + getMoneyName(type) +
                        ", cơ hội trúng của bạn là: " + DecisionMakerService.getPercent(player, type, true) + "%\n"
                        + "Tổng giải VIP: " + Util.numberToMoney(totalVIP) + " " + getMoneyName(type) +
                        ", cơ hội trúng của bạn là: " + DecisionMakerService.getPercent(player, type, false) + "%\n"
                        + "Thời gian còn lại: " + DecisionMakerCost.timeGame + " giây.",
                "Cập nhật",
                getUnitNormal(type),
                getUnitVIP(type),
                "Đóng"
        );
    }

    // ================= PLAY =================
    public static void selectPlay(Npc npc, Player player, int type, boolean isNormal) {
        long cost = getCost(type, isNormal);

        if (!hasEnoughMoney(player, type, cost)) {
            Service.gI().sendThongBao(player,
                    "Bạn không đủ " + getMoneyName(type) +
                            ", còn thiếu " + (cost - getCurrentMoney(player, type)));
            return;
        }

        deductMoney(player, type, cost);
        Service.gI().sendMoney(player);

        DecisionMakerService.newData(player, cost, (byte) type, isNormal);
        showMenuSelect(npc, player, type);
    }

    // ================= HELPER =================

    private static int getMenuId(int type) {
        return switch (type) {
            case DecisionMakerCost.VANG -> ConstMiniGame.MENU_PLAY_DECISION_GOLD;
            case DecisionMakerCost.NGOC_XANH -> ConstMiniGame.MENU_PLAY_DECISION_GEM;
            case DecisionMakerCost.HONG_NGOC -> ConstMiniGame.MENU_PLAY_DECISION_RUBY;
            default -> -1;
        };
    }

    private static String getMoneyName(int type) {
        return switch (type) {
            case DecisionMakerCost.VANG -> "vàng";
            case DecisionMakerCost.NGOC_XANH -> "ngọc xanh";
            case DecisionMakerCost.HONG_NGOC -> "hồng ngọc";
            default -> "tiền";
        };
    }

    private static String getUnitNormal(int type) {
        return switch (type) {
            case DecisionMakerCost.VANG -> "Thường\n1 triệu\nvàng";
            case DecisionMakerCost.NGOC_XANH -> "Thường\n10 ngọc\nxanh";
            case DecisionMakerCost.HONG_NGOC -> "Thường\n10 hồng\nngọc";
            default -> "Thường";
        };
    }

    private static String getUnitVIP(int type) {
        return switch (type) {
            case DecisionMakerCost.VANG -> "VIP\n10 triệu\nvàng";
            case DecisionMakerCost.NGOC_XANH -> "VIP\n100 ngọc\nxanh";
            case DecisionMakerCost.HONG_NGOC -> "VIP\n100 hồng\nngọc";
            default -> "VIP";
        };
    }

    // ⚠️ FIX: dùng long
    private static long getCost(int type, boolean isNormal) {
        return switch (type) {
            case DecisionMakerCost.VANG ->
                    isNormal ? DecisionMakerCost.COST_GOLD_NORMAL : DecisionMakerCost.COST_GOLD_VIP;

            case DecisionMakerCost.NGOC_XANH ->
                    isNormal ? DecisionMakerCost.COST_GEM_NORMAL : DecisionMakerCost.COST_GEM_VIP;

            case DecisionMakerCost.HONG_NGOC ->
                    isNormal ? DecisionMakerCost.COST_RUBY_NORMAL : DecisionMakerCost.COST_RUBY_VIP;

            default -> 0;
        };
    }

    // ⚠️ FIX: trả về long
    private static long getCurrentMoney(Player player, int type) {
        return switch (type) {
            case DecisionMakerCost.VANG -> player.inventory.gold;
            case DecisionMakerCost.NGOC_XANH -> player.inventory.gem;
            case DecisionMakerCost.HONG_NGOC -> player.inventory.ruby;
            default -> 0;
        };
    }

    private static boolean hasEnoughMoney(Player player, int type, long amount) {
        return getCurrentMoney(player, type) >= amount;
    }

 
    private static void deductMoney(Player player, int type, long amount) {
        switch (type) {
            case DecisionMakerCost.VANG -> player.inventory.gold -= amount;
            case DecisionMakerCost.NGOC_XANH -> player.inventory.gem -= amount;
            case DecisionMakerCost.HONG_NGOC -> player.inventory.ruby -= amount;
        }
    }
}