package minigame.DecisionMaker;

import consts.ConstMiniGame;
import consts.ConstNpc;
import database.NTTSqlFetcher;
import npc.Npc;
import player.Player;
import server.Maintenance;
import minigame.cost.*;
import utils.Util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DecisionMaker implements Runnable {

    private static DecisionMaker instance;

    public static DecisionMaker gI() {
        if (instance == null) {
            instance = new DecisionMaker();
        }
        return instance;
    }

    public static volatile boolean spinGame;
    public static boolean delayNewGame;

    static {
        DecisionMakerCost.timeGame = DecisionMakerCost.timeGameDefalue;
        spinGame = true;
        delayNewGame = false;
    }

    public List<DecisionMakerData> listPlayer = new ArrayList<>();
    public List<DecisionMakerData.resulPlayer> listResulPlayer = new ArrayList<>();

    @Override
    public void run() {
        while (!Maintenance.isRunning) {
            try {

                // ===== COUNTDOWN GAME =====
                if (DecisionMakerCost.timeGame > 0) {
                    DecisionMakerCost.timeGame--;

                    if (DecisionMakerCost.timeGame == 0 && spinGame) {
                        spinGame = false; // 🔥 tránh gọi nhiều lần
                        spinGame();
                    }
                }

                // ===== DELAY RESET =====
                if (DecisionMakerCost.timeDelay > 0) {
                    DecisionMakerCost.timeDelay--;

                    if (DecisionMakerCost.timeDelay == 0) {
                        resetNewGame();
                    }
                }

                Thread.sleep(1000); // ✅ fix sleep

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // ================= SPIN =================
    public void spinGame() {

        playerWin(DecisionMakerCost.VANG, true);
        playerWin(DecisionMakerCost.VANG, false);

        playerWin(DecisionMakerCost.NGOC_XANH, true);
        playerWin(DecisionMakerCost.NGOC_XANH, false);

        playerWin(DecisionMakerCost.HONG_NGOC, true);
        playerWin(DecisionMakerCost.HONG_NGOC, false);

        delayNewGame = true;
        DecisionMakerCost.timeDelay = 60;
    }

    // ================= RESET =================
    public void resetNewGame() {
        DecisionMakerCost.timeGame = DecisionMakerCost.timeGameDefalue;
        spinGame = true;
        delayNewGame = false;
        listPlayer.clear(); // 🔥 reset người chơi
        listResulPlayer.clear();
    }

    // ================= WIN =================
    public void playerWin(long TYPE, boolean isNormal) {

        List<DecisionMakerData> listPl = new ArrayList<>();

        if (!listPlayer.isEmpty()) {

            listPlayer.sort(Comparator.comparingLong(o -> o.money)); // ✅ fix long

            for (DecisionMakerData pl : listPlayer) {
                if (pl.type == TYPE && pl.isNormal == isNormal) {
                    listPl.add(pl);
                }
            }
        }

        if (!listPl.isEmpty()) {
            int index = Util.nextInt(0, listPl.size() - 1);
            DecisionMakerData data = listPl.get(index);

            Player player =NTTSqlFetcher.loadById(data.id);

            if (player != null) {
                DecisionMakerService.newDataResul(player, (byte) TYPE, data.money);
            }
        }
    }

    // ================= MENU WAIT =================
    public void showMenuWaitNewGame(Npc npc, Player player) {
        String npcSay = "Chúc mừng các bạn may mắn được chọn lần trước là";

        for (DecisionMakerData.resulPlayer pl : listResulPlayer) {
            String giatri = switch (pl.type) {
                case DecisionMakerCost.VANG -> " vàng";
                case DecisionMakerCost.NGOC_XANH -> " ngọc xanh";
                case DecisionMakerCost.HONG_NGOC -> " hồng ngọc";
                default -> "";
            };

            npcSay += "\n" + pl.name + " +" + Util.numberToMoney(pl.money) + giatri;
        }

        npcSay += "\nTrò chơi sẽ bắt đầu sau: " + DecisionMakerCost.timeDelay + " giây nữa.";

        npc.createOtherMenu(player,
                ConstMiniGame.MENU_WAIT_NEW_GAME,
                npcSay,
                "Thể lệ",
                "OK");
    }

    // ================= MENU =================
    public void showMenu(Npc npc, Player player) {

        if (delayNewGame && DecisionMakerCost.timeDelay > 0) {
            showMenuWaitNewGame(npc, player);
            return;
        }

        npc.createOtherMenu(player,
                ConstMiniGame.MENU_CHON_AI_DAY,
                "Trò chơi Chọn Ai Đây đang được diễn ra, nếu bạn tin tưởng mình đang tràn đầy may mắn thì có thể tham gia thử.",
                "Thể lệ",
                "Chọn\nVàng",
                "Chọn\nHồng ngọc",
                "Chọn\nNgọc xanh");
    }

    // ================= TUTORIAL =================
    public void showTutorial(Npc npc, Player player) {
        npc.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                "Mỗi lượt chơi có 6 giải thưởng\n"
                        + "Được chọn tối đa 10 lần mỗi giải\n"
                        + "Thời gian 1 lượt chọn là 5 phút\n"
                        + "Khi hết giờ, hệ thống sẽ ngẫu nhiên chọn ra 1 người may mắn\n"
                        + "của từng giải và trao thưởng.\n"
                        + "Lưu ý: Nếu tham gia bằng Ngọc xanh hoặc Hồng ngọc\n"
                        + "thì người thắng sẽ nhận Hồng ngọc.",
                "OK");
    }
}