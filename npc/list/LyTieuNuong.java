package npc.list;

import consts.ConstMiniGame;
import consts.ConstNpc;
import database.PlayerDAO;
import minigame.DecisionMaker.DecisionMaker;
import minigame.DecisionMaker.DecisionMakerHandler;
 import minigame.LuckyNumber.LuckyNumber;
import minigame.LuckyNumber.LuckyNumberService;
import minigame.RockPaperScissors.RockPaperScissors;
import minigame.cost.DecisionMakerCost;
import minigame.cost.LuckyNumberCost;
import npc.Npc;
import player.Player;
import services.Service;
import services.TaskService;
import services.func.Input;
import player.Service.InventoryService;

public class LyTieuNuong extends Npc {

     

    public LyTieuNuong(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    // ================= MENU =================
    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player) && !TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
            createOtherMenu(player, ConstMiniGame.MENU_MAIN,
                    "|0|Ngọc Rồng 2025",
                    "Chức năng",
                    "Mini Game",
                    "Đóng");
        }
    }

    // ================= HANDLE =================
    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) return;

        switch (player.idMark.getIndexMenu()) {

            // ===== MENU CHÍNH =====
            case ConstMiniGame.MENU_MAIN -> {
                if (select == 0) {
                    createOtherMenu(player, ConstMiniGame.MENU_SHOP,
                            "Chức năng",
                            "Mua thành viên",
                            "Đổi thỏi vàng");
                } 
                if (select == 1) {
                    createOtherMenu(player, ConstMiniGame.MENU_MINIGAME,
                            "Mini Game",
                            "Kéo búa bao",
                            "Con số may mắn",
                            "Chọn ai đây");
                }
            }

            // ===== SHOP =====
            case ConstMiniGame.MENU_SHOP -> {
                switch (select) {
                    case 0 -> {
                        if (!player.getSession().actived) {
                            if (player.getSession().vnd >= 10000) {
                                player.getSession().actived = true;
                                if (PlayerDAO.MuaThanhVien(player, 0)) {
                                    InventoryService.gI().sendItemBags(player);
                                    Service.gI().sendMoney(player);
                                    npcChat(player, "Mua thành viên thành công!");
                                }
                            } else {
                                npcChat(player, "Không đủ tiền!");
                            }
                        } else {
                            npcChat(player, "Đã kích hoạt rồi!");
                        }
                    }
                    case 1 -> Input.gI().createFormTradeGold(player);
                }
            }

            // ===== MINI GAME =====
            case ConstMiniGame.MENU_MINIGAME -> {
                switch (select) {
                    case 0 -> createOtherMenu(player, ConstMiniGame.MENU_KEO_BUA_BAO,
                            "Chọn cược", "1Tr", "5Tr", "10Tr");

                    case 1 -> {
                        LuckyNumber.showMenu(this, player, false);
                        player.idMark.setGemCSMM(false);
                    }

                    case 2 -> DecisionMaker.gI().showMenu(this, player);
                }
            }

            // ===== KÉO BÚA BAO =====
            case ConstMiniGame.MENU_KEO_BUA_BAO ->
                RockPaperScissors.confirmMenu(this, player, select);

            case ConstMiniGame.MENU_PLAY_KEO_BUA_BAO -> {
                if (player.idMark.getTimePlayKeoBuaBao() - System.currentTimeMillis() > 0) {
                    RockPaperScissors.confirmPlay(this, player, select);
                } else {
                    
                    createOtherMenu(player, ConstMiniGame.MENU_KEO_BUA_BAO,
                            "Thời gian chọn đã hết, vui lòng chọn cược lại", "1Tr", "5Tr", "10Tr");
                }
            }

            // ===== CSMM =====
            case ConstMiniGame.MENU_PLAY_LUCKY_NUMBER_GOLD,
                 ConstMiniGame.MENU_PLAY_LUCKY_NUMBER_GEM -> {

                switch (select) {
                    case 0 -> LuckyNumber.showMenu(this, player, player.idMark.isGemCSMM());
                    case 1 -> Input.gI().createFormSelectOneNumberLuckyNumber(player, player.idMark.isGemCSMM());
                    case 2 -> LuckyNumberService.addOneNumber(player, true);
                    case 3 -> LuckyNumberService.addOneNumber(player, false);
                }
            }

            // ===== CHỌN AI =====
            case ConstMiniGame.MENU_CHON_AI_DAY -> {
                switch (select) {
                    case 0 -> DecisionMakerHandler.showMenuSelect(this, player,LuckyNumberCost.costGold);
                    case 1 -> DecisionMakerHandler.showMenuSelect(this, player,LuckyNumberCost.costGem);
                    case 2 -> DecisionMakerHandler.showMenuSelect(this, player,DecisionMakerCost.HONG_NGOC);
                }
            }
        }
    }
}