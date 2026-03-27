package matches.The23rdMartialArtCongress;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClanRankBuilder {

    private long clanid;
    private int rank;
    private long lastPKTime;
    private long lastTimeReward;
     private int win;
    private int lose;
    private String info;

    private int head;
    private int body;
    private int leg;
    private String name;

    public void dispose() {
        name = null;
        info = null;
    }
}