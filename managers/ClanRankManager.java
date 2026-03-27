package managers;

import matches.The23rdMartialArtCongress.ClanRank;
import utils.Functions;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lombok.NonNull;
import map.Zone;
import item.Template.WaitClanRank;
import matches.The23rdMartialArtCongress.ClanRankService;
import player.Player;
import server.Client;
import server.Maintenance;

public class ClanRankManager implements Runnable {

    private final List<WaitClanRank> waitList;
    private final List<ClanRank> list;
    private static ClanRankManager instance;

    public static ClanRankManager gI() {
        if (instance == null) {
            instance = new ClanRankManager();
        }
        return instance;
    }

    public ClanRankManager() {
        waitList = new ArrayList<>();
        list = new ArrayList<>();
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning) {
            long startTime = System.currentTimeMillis();

            try {
                Iterator<WaitClanRank> iterator = waitList.iterator();

                while (iterator.hasNext()) {
                    WaitClanRank wsp = iterator.next();

                    System.out.println("[ClanRankManager] xử lý: "
                            + wsp.playerclanId + " vs " + wsp.rivalclanId);

                    // 🔥 LẤY PLAYER THEO CLAN
                    Player player = getPlayerByClanId(wsp.playerclanId);

                    if (player == null || player.zone == null || player.zone.map == null) {
                        System.out.println(" - Player null hoặc chưa load map");
                        iterator.remove();
                        continue;
                    }

                    // 🔥 CHỈ CHO Ở MAP 13
                    if (player.zone.map.mapId != 13) {
                        System.out.println(" - Không ở map 13");
                        iterator.remove();
                        continue;
                    }

                    // 🔥 CHẶN GỌI LẠI
                    if (player.isPKClanRank) {
                        System.out.println(" - Đang trong trận ClanRank");
                        iterator.remove();
                        continue;
                    }

                    // 🔥 CHECK TRẠNG THÁI
                    if (currentlyCompeting(player.clan.id)
                            || awaitingCompetition(player.clan.id)) {
                        System.out.println(" - Clan đang thi đấu hoặc chờ");
                        iterator.remove();
                        continue;
                    }

                    // 🔥 CHECK ZONE HIỆN TẠI
                    if (SPRCheck(player.zone)) {
                        System.out.println(" - Zone đã có trận");
                        continue;
                    }

                    try {
                        // 🔥 LẤY ZONE THI ĐẤU
                        Zone zone = ClanRankService.gI().getZone(165);

                        if (zone == null) {
                            System.out.println(" - Không có zone thi đấu");
                            iterator.remove();
                            continue;
                        }

                        // 🔥 TẠO TRẬN
                        ClanRank match = new ClanRank(player, wsp.rivalclanId, zone);

                        if (match == null || match.getRivalClan() == null) {
                            System.out.println(" - Lỗi tạo match");
                            iterator.remove();
                            continue;
                        }

                        // 🔥 SET FLAG TRƯỚC
                        player.isPKClanRank = true;

                        // 🔥 ADD LIST
                        list.add(match);

                        System.out.println("[ClanRankManager] Tạo trận: "
                                + player.name + " vs " + match.getRivalClan().name);

                        // 🔥 START TRẬN
                        match.startMatch();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    iterator.remove();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            long sleep = 500 - (System.currentTimeMillis() - startTime);
            Functions.sleep(Math.max(sleep, 10));
        }
    }

    // 🔥 LẤY PLAYER THEO CLAN
    public Player getPlayerByClanId(long clanId) {
        for (Player pl : Client.gI().getPlayers()) {
            if (pl != null && pl.clan != null && pl.clan.id == clanId) {
                return pl;
            }
        }
        return null;
    }
    
    public String getCompeting(long clanId) {
    for (int i = list.size() - 1; i >= 0; i--) {
        ClanRank clr = list.get(i);

        if (clr == null || clr.getZone() == null) continue;

        // 👉 nếu là clan người chơi
        if (clr.getPlayerId() == clanId) {
            return "VS " 
                + (clr.getRivalClan() != null ? clr.getRivalClan().name : "??")
                + " kv: " + clr.getZone().zoneId;
        }

        // 👉 nếu là clan đối thủ
        if (clr.getRivalId() == clanId) {
            return "VS "
                + (clr.getClan() != null ? clr.getClan().name : "??")
                + " kv: " + clr.getZone().zoneId;
        }
    }
    return "";
}

    public boolean currentlyCompeting(long clanId) {
        for (int i = list.size() - 1; i >= 0; i--) {
            ClanRank clr = list.get(i);
            if (clr.getPlayerId() == clanId || clr.getRivalId() == clanId) {
                return true;
            }
        }
        return false;
    }

    public boolean awaitingCompetition(long clanId) {
        for (int i = waitList.size() - 1; i >= 0; i--) {
            WaitClanRank wclr = waitList.get(i);
            if (wclr.playerclanId == clanId || wclr.rivalclanId == clanId) {
                return true;
            }
        }
        return false;
    }

    public boolean SPRCheck(@NonNull Zone zone) {
        for (int i = list.size() - 1; i >= 0; i--) {
            ClanRank clr = list.get(i);
            if (clr.getZone() != null && clr.getZone().equals(zone)) {
                return true;
            }
        }
        return false;
    }

    public void addSPR(ClanRank clr) {
        if (clr == null || clr.getRivalClan() == null || clr.getPlayer() == null) {
            return;
        }
        list.add(clr);
    }

    public void removeSPR(ClanRank clr) {
        list.remove(clr);
    }

    public void addWSPR(long clanId, long rivalClanId) {
        waitList.add(new WaitClanRank(clanId, rivalClanId));
        System.out.println("[ClanRankManager] thêm chờ: " + clanId + " vs " + rivalClanId);
    }
}