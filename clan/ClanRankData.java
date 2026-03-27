package clan;

import java.util.ArrayList;
import java.util.List;

import matches.The23rdMartialArtCongress.ClanRankService;
import services.Service;
import player.Player;

public class ClanRankData {

    private Clan clan;
    public int rank;
    public int win;
    public int lose;

    public List<Long> lastTime;
    public long lastPKTime;
    public long lastRewardTime;

    public ClanRankData(Clan clan) {
        this.clan = clan;
        this.lastTime = new ArrayList<>();
    }

    
   public void reward() {
    int rw = ClanRankService.gI().reward(rank); // số ngọc thưởng theo hạng
    if (rw != -1 && clan != null) {
        for (Player pl : clan.membersInGame) { // trả về danh sách tất cả thành viên đang online
            if (pl != null) {
                Service.gI().sendThongBao(pl, "Bang của bạn đang ở TOP " + rank + " võ đài  mỗi thành viên nhận " + rw + " ngọc xanh");
                pl.inventory.gem += rw;
                Service.gI().sendMoney(pl);
            }
        }
    }
    lastRewardTime = System.currentTimeMillis();
}


    public void dispose() {
        lastTime.clear();
        win = -1;
        lose = -1;
        lastPKTime = -1;
    }
}
