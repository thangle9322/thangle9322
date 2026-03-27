
/*
 * Copyright by EMTI
 */
package minigame.RockPaperScissors;

import consts.ConstFont;
import consts.ConstMiniGame;
import consts.ConstNpc;
import consts.ConstTournament;
import map.Service.NpcService;
import npc.Npc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import player.Player;
import services.ItemTimeService;
import services.Service;
import utils.Util;

public class RockPaperScissors {

    public static final byte KEO = 0;
    public static final byte BUA = 1;
    public static final byte BAO = 2;
    private static final Logger log = LoggerFactory.getLogger(RockPaperScissors.class);

    public static long timePlay = 15;

    public static int COST_0 = 1_000_000;
    public static int COST_1 = 5_000_000;
    public static int COST_2 = 10_000_000;

    public static void confirmMenu(Npc npc, Player player, int select) {

        int tiendatcuoc = (select == 0 ? COST_0 : select == 1 ? COST_1 : COST_2);
        String money = Util.numberFormatTanTai(tiendatcuoc);

        player.idMark.setMoneyKeoBuaBao(tiendatcuoc);
        player.idMark.setTimePlayKeoBuaBao(System.currentTimeMillis() + (timePlay * 1000));
        ItemTimeService.gI().sendTextTimeKeoBuaBao(player, (int) timePlay);

        npc.createOtherMenu(player, ConstMiniGame.MENU_PLAY_KEO_BUA_BAO,
                ConstFont.BOLD_GREEN + "Mức vàng cược: " + money + "\n"
                + ConstFont.BOLD_DARK + "Hãy chọn Kéo, Búa hoặc Bao\n"
                + ConstFont.BOLD_RED + "Thời gian còn: " + timePlay + " giây",
                "Kéo", "Búa", "Bao", "Đổi\nmức cược", "Nghỉ chơi");

    }
    public static void confirmPlay(Npc npc, Player player, int select) {

        switch (select) {
            case 0, 1, 2:
                 
 
                // check vàng
                if (player.inventory.gold < player.idMark.getMoneyKeoBuaBao()) {
                    long thieu = player.idMark.getMoneyKeoBuaBao() - player.inventory.gold;
                    Service.gI().sendThongBao(player,
                            "Bạn không đủ vàng, còn thiếu " + Util.numberToMoney(thieu));
                    return;
                }

                int playerPick = select;
                int serverPick = Util.nextInt(0, 2);

                player.idMark.setKeoBuaBaoPlayer((byte) playerPick);
                player.idMark.setKeoBuaBaoServer((byte) serverPick);

                //effect player
                Service.gI().sendEffAllPlayer(player, 1000 + playerPick, 1, 2, 1);

                // 🎬 npc
                Util.delay(300, () -> {
                    Service.gI().sendEffAllPlayer(player, 1000 + serverPick, 1, 2, 1);
                });

                int result = RockPaperScissorsService.checkWinLose(playerPick, serverPick);

                // 🎯 show result
                Util.delay(600, () -> {
                    RockPaperScissorsService.showResult(npc, player, result);

                    if (result == 1) {
                        Service.gI().sendEffAllPlayer(player, 1100, 1, 1, 1);
                    } else if (result == 2) {
                        Service.gI().sendEffAllPlayer(player, 1200, 1, 1, 1);
                    }
                });

                break;

            case 3:
                npc.createOtherMenu(player, ConstMiniGame.MENU_KEO_BUA_BAO,
                        "Hãy chọn mức cược.",
                        "1 Tr vàng",
                        "5 Tr vàng",
                        "10 Tr vàng");
                break;
        }
    }
}
