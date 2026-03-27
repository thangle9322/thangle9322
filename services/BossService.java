package services;

/*
 * @Author: DienCoLamCoi
 * @Description: Điện Cơ Lâm Còi - Chuyên cung cấp thiết bị điện cơ uy tín chất lượng cao.
 * @Group Zalo: Giao lưu chia sẻ kinh nghiệm code - https://zalo.me/g/lsqfzx907
 */


import boss.Boss;
import boss.BossID;
import boss.boss_list_call.Black.Black_Goku;
import boss.boss_list_call.Black.SUPER_BLACK_GOKU;
import boss.boss_list_call.Broly.Broly;
import boss.boss_list_call.Broly.SuperBroly;
import boss.boss_list_call.Nappa.Kuku;
  
import player.Player;
import utils.Logger;

public class BossService {

    private static BossService instance;

    public static BossService gI() {
        if (instance == null) {
            instance = new BossService();
        }
        return instance;
    }

     public Boss callBoss(Player pl, int bossID) {
        try {
            switch (bossID) {
                case BossID.BROLY -> {
                    return new Broly(pl);
                }
                case BossID.SUPER_BROLY -> {
                    return new SuperBroly(pl.zone,pl.location.x,pl.location.y);
                }
                case BossID.SUPER_BLACK_GOKU -> {
                    return new SUPER_BLACK_GOKU(pl);
                }
                case BossID.BLACK_GOKU -> {
                    return new Black_Goku(pl);
                }
                case BossID.KUKU -> {
                    return new Kuku(pl);
                }
                default -> {
                    Service.gI().sendThongBao(pl, "ID boss không hợp lệ: " + bossID);
                }
            }
        } catch (Exception e) {
            Logger.logException(BossService.class, e);
        }
        return null;
    }

     
}
