package matches.The23rdMartialArtCongress;

import clan.Clan;
import clan.ClanMember;
import consts.ConstClanRank;
import consts.ConstSuperRank;
import java.util.List;
import database.ClanRankDAO;
import managers.ClanRankManager;
import map.Map;
import map.Zone;
import network.Message;
import player.Player;
import server.Client;
import map.Service.MapService;
import player.Service.ClanService;
import services.Service;
import utils.Logger;

public class ClanRankService {

    private static ClanRankService instance;

    public static ClanRankService gI() {
        if (instance == null) {
            instance = new ClanRankService();
        }
        return instance;
    }
     
     public void competing(Player player, long clanId) {
    try {

        Logger.primaryln("[ClanRankService] >>> competing(playerId=" + player.id + ", clanId=" + clanId + ")");

        if (player.isPKClanRank) {
            Logger.primaryln("BLOCK CALL 2 LAN");
            return;
        }
        // ===== CHẶN SAI =====
        if (  player.zone == null || player.zone.map == null) {
            Logger.primaryln("[ClanRankService] player hoặc map null");
            return;
        }

        if (player.zone.map.mapId != 13 || clanId <= 0) {
            Logger.primaryln("[ClanRankService] map sai hoặc clanId lỗi: " + clanId);
            return;
        }

        int menuType = player.idMark.getMenuType();

        // ===== LOAD CLAN =====
        Clan rivalClan = ClanService.gI().getClanById((int) clanId);
        if (rivalClan == null) {
            Logger.primaryln("[ClanRankService] rivalClan null");
            Service.gI().sendThongBao(player, "Không tìm thấy bang!");
            return;
        }

        // ===== LOAD DATA =====
        try {
            rivalClan.reloadClanMember();
            ClanRankDAO.loadDataClan(rivalClan);
            ClanRankDAO.loadData(player);
        } catch (Exception e) {
            Logger.primaryln("[ClanRankService] Lỗi load clan/rank");
            e.printStackTrace();
            Service.gI().sendThongBao(player, "Lỗi load dữ liệu bang!");
            return;
        }

        // ===== LẤY RANK =====
        int myRank = (player.clan != null && player.clan.clanRank != null) ? player.clan.clanRank.rank : -1;
        int rivalRank = (rivalClan.clanRank != null) ? rivalClan.clanRank.rank : -1;

        Logger.primaryln("MY RANK: " + myRank);
        Logger.primaryln("RIVAL RANK: " + rivalRank);

        if (myRank <= 0 || rivalRank <= 0) {
            Service.gI().sendThongBao(player, "Clan chưa có rank!");
            return;
        }

        // ===== CHECK TRẠNG THÁI =====
        try {
            if (ClanRankManager.gI().currentlyCompeting(player.clan.id)) {
                Service.gI().sendThongBao(player, ConstClanRank.TEXT_DANG_THI_DAU);
                return;
            } else if (ClanRankManager.gI().currentlyCompeting(rivalClan.id)) {
                Service.gI().sendThongBao(player, ConstClanRank.TEXT_DOI_THU_DANG_THI_DAU);
                return;
            } else if (ClanRankManager.gI().awaitingCompetition(player.clan.id)) {
                Service.gI().sendThongBao(player, ConstClanRank.TEXT_DANG_CHO);
                return;
            } else if (ClanRankManager.gI().awaitingCompetition(rivalClan.id)) {
                Service.gI().sendThongBao(player, ConstClanRank.TEXT_DOI_THU_CHO_THI_DAU);
                return;
            }
        } catch (Exception e) {
            Logger.primaryln("[ClanRankService] Lỗi check trạng thái");
            e.printStackTrace();
            return;
        }

        // ===== CHECK LOGIC =====
        if (myRank < rivalRank) {
            Service.gI().sendThongBao(player, "Không thể thách bang trên hạng!");
            return;
        } else if (myRank == rivalRank || rivalClan.id == player.clan.id) {
            Service.gI().sendThongBao(player, "Không thể thách chính bang!");
            return;
        } else if (rivalRank <= 10 && Math.abs(myRank - rivalRank) > 2) {
            Service.gI().sendThongBao(player, "Chỉ được thách trong ±2 hạng!");
            return;
        }

        // ===== TẠO TRẬN =====
         
        try {
            switch (menuType) {

                case 0 -> {
                    Service.gI().sendThongBao(player, ConstSuperRank.TEXT_TOP_100);
                }
 
                case 1 -> {
                    Zone zone = getZone(165);
                    if (zone != null) {
                        ClanRank match = new ClanRank(player, clanId, zone);
                        ClanRankManager.gI().addSPR(match);
                        player.isPKDHVT = true;
                        match.startMatch();
                    }
                }
            }

        } catch (Exception e) {
            Logger.primaryln("[ClanRankService] Lỗi tạo trận");
            e.printStackTrace();
            Service.gI().sendThongBao(player, "Lỗi tạo trận!");
        }

    } catch (Exception e) {
        Logger.primaryln("[ClanRankService] LỖI NGHIÊM TRỌNG competing()");
        e.printStackTrace();
    }
}

    public void topList(Player player, int type) {
        long st = System.currentTimeMillis();
        player.idMark.setMenuType(type);
       
        Message msg = null;
        try {
            List<ClanRankBuilder> list = (type == 0) ? ClanRankDAO.getPlayerListInRank(player.clan.clanRank.rank, 100)
                        : ClanRankDAO.getPlayerListInRankRange(player.clan.clanRank.rank, 11);
             msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Top 100 Bang Hội");
 
            msg.writer().writeByte(list.size());
            for (int i = 0; i < list.size(); i++) {
                ClanRankBuilder sb = list.get(i);
                 
                msg.writer().writeInt(sb.getRank());
                msg.writer().writeInt((int) sb.getClanid());
                msg.writer().writeShort(sb.getHead());
                if (214 < player.getSession().version) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(sb.getBody());
                msg.writer().writeShort(sb.getLeg());
                msg.writer().writeUTF(sb.getName());
                msg.writer().writeUTF(textStatus(sb));
                msg.writer().writeUTF(sb.getInfo());
        }
            player.sendMessage(msg);
            msg.cleanup();
            for (ClanRankBuilder sb : list) {
                sb.dispose();
            }
            list.clear();
        } catch (Exception e) {
            Logger.primaryln("Lỗi khi lấy top clan: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
        Logger.primaryln("Thời gian xử lý topList: " + (System.currentTimeMillis() - st) + " ms");
    }
     
     public Clan loadClan(int clanId) {
    try {
        if (clanId <= 0) {
            System.out.println("[loadClan] clanId lỗi: " + clanId);
            return null;
        }

        // 🔥 1. LẤY CLAN TỪ MANAGER (ONLINE)
        Clan clan = ClanService.gI().getClanById(clanId);

        

        if (clan == null) {
            System.out.println("[loadClan] Không tìm thấy clanId=" + clanId);
            return null;
        }

        // 🔥 3. LOAD MEMBER
        if (clan.getMembers() == null || clan.getMembers().isEmpty()) {
            clan.reloadClanMember();
        }

        // 🔥 4. LOAD RANK
        if (clan.clanRank == null || clan.clanRank.rank <= 0) {
            ClanRankDAO.loadDataClan(clan);
        }

        // 🔥 5. CHECK LEADER
        if (clan.getLeader() == null) {
            System.out.println("[loadClan] Clan không có leader!");
        }

        return clan;

    } catch (Exception e) {
        System.err.println("[loadClan] Lỗi: " + e.getMessage());
        e.printStackTrace();
        return null;
    }
}
     
    public String textInfo(Player pl) {
        pl.setClothes.setup();
        if (pl.pet != null) {
            pl.pet.setClothes.setup();
        }

        StringBuilder text = new StringBuilder();

        if (pl.clan != null) {
            ClanMember leader = pl.clan.getLeader();
            if (leader != null) {
                text.append("Bang chủ: ").append(leader.name).append("\n");
            } else {
                text.append("Bang chủ: Không xác định\n");
            }
        } else {
            text.append("Không có dữ liệu\n");
        }

        return text.toString();
    }

    public String textInfoNew(Player pl) {
        if (pl == null || pl.clan == null) {
            return "Không xác định!";
        }
        pl.setClothes.setup();
        if (pl.pet != null) {
            pl.pet.setClothes.setup();
        }
        StringBuilder text = new StringBuilder();
        if (pl.clan != null) {
            ClanMember leader = pl.clan.getLeader();
            if (leader != null) {
                text.append("Bang chủ: ").append(leader.name).append("\n");
            } else {
                text.append("Bang chủ: Không xác định\n");
            }
        } else {
            text.append("Không có dữ liệu bang hội\n");
        }

        return text.toString();
    }

    public String textStatus(ClanRankBuilder srb) {
       if (ClanRankManager.gI().currentlyCompeting(srb.getClanid())) {
            return ClanRankManager.gI().getCompeting(srb.getClanid());
        }
        return textReward(srb.getRank());
    }

    public String textReward(int rank) {
        String text = "";
        if (rank == 1) {
            text = "+100 ngọc/ ngày";
        } else if (rank >= 2 && rank <= 10) {
            text = "+20 ngọc/ ngày";
        } else if (rank >= 11 && rank <= 100) {
            text = "+5 ngọc/ ngày";
        } else if (rank >= 101 && rank <= 1000) {
            text = "+1 ngọc/ ngày";
        }
        return text;
    }

    public int reward(int rank) {
        int rw = -1;
        if (rank == 1) {
            rw = 100;
        } else if (rank >= 2 && rank <= 10) {
            rw = 20;
        } else if (rank >= 11 && rank <= 100) {
            rw = 5;
        } else if (rank >= 101 && rank <= 1000) {
            rw = 1;
        }
        return rw;
    }

    public Zone getZone(int mapId) {

        Map map = MapService.gI().getMapById(mapId);
        try {
            if (map != null) {
                int zoneId = 0;
                while (zoneId < map.zones.size()) {
                    Zone zonez = map.zones.get(zoneId);
                    if (!ClanRankManager.gI().SPRCheck(zonez)) {
                        Logger.primaryln("Chọn zone: " + zonez.zoneId + " của map " + mapId);
                        return zonez;
                    }
                    zoneId++;
                }
            } else {
                Logger.primaryln("Không tìm thấy mapId: " + mapId);
            }
        } catch (Exception e) {
            Logger.primaryln("Lỗi khi tìm zone: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
