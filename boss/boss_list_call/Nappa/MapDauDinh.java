package boss.boss_list_call.Nappa;

/*
 * @Author: DienCoLamCoi
 * @Description: Điện Cơ Lâm Còi - Chuyên cung cấp thiết bị điện cơ uy tín chất lượng cao.
 * @Group Zalo: Giao lưu chia sẻ kinh nghiệm code - https://zalo.me/g/lsqfzx907
 */


import boss.Boss;
import boss.BossID;
import consts.BossStatus;
import boss.BossesData;
import boss.boss_list_call.Training.Boss_call;
import utils.Util;

public class MapDauDinh extends Boss_call {

    private long st;

    public MapDauDinh() throws Exception {
        super(BossID.MAP_DAU_DINH, true, true, BossesData.MAP_DAU_DINH);
    }

    @Override
    public void joinMap() {
        super.joinMap();
        st = System.currentTimeMillis();
    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, 900000)) {
            this.changeStatus(BossStatus.LEAVE_MAP);
        }
//        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
//            st = System.currentTimeMillis();
//        }
    }
}
