package matches.The23rdMartialArtCongress;

import boss.Boss;
import boss.Clan_rank.Rival;
import clan.Clan;
import clan.ClanMember;
import consts.BossStatus;
import consts.ConstClanRank;
import consts.ConstPlayer;
import database.ClanRankDAO;
import lombok.Data;
import managers.ClanRankManager;
import map.Zone;
import player.Player;
import player.Service.PlayerService;
import services.Service;
import map.Service.ChangeMapService;
import server.Client;
import server.Maintenance;
import utils.Functions;
import utils.Util;

@Data
public final class ClanRank implements Runnable {

    private Zone zone;
    private boolean isOpened;
    private Clan clan;
    private Clan rivalClan;
    private long playerId;
    private long rivalId;

    private Player player;
    private Boss rival;

    public int rankWin;
    public int rankLose;

    public boolean win;
    public int timeUp;
    public int timeDown;
    public int error;

    public ClanRank(Player player, long rivalClanId, Zone zone) {
        try {
            this.zone = zone;
            this.player = player;
            this.clan = player.clan;
            this.playerId = clan.id;

            this.rivalId = rivalClanId;
            this.rivalClan = ClanRankService.gI().loadClan((int) rivalClanId);

            if (clan == null || rivalClan == null) {
                throw new RuntimeException("Clan null");
            }

            // LOAD RANK
            ClanRankDAO.loadDataClan(clan);
            ClanRankDAO.loadDataClan(rivalClan);

            this.rankLose = clan.clanRank.rank;
            this.rankWin = rivalClan.clanRank.rank;

            // ===== ĐẢM BẢO PLAYER ĐÚNG MAP =====
            if (player.zone.zoneId != zone.zoneId) {
                ChangeMapService.gI().changeZone(player, zone.zoneId);
            }

            // tạo boss
            Player temp = player;
            if (rivalClan.getLeader() != null) {
                Player lead = Client.gI().getPlayer(rivalClan.getLeader().id);
                if (lead != null) {
                    temp = lead;
                }
            }

            this.rival = new Rival(player, temp);

            // 🔥 SPAWN RIVAL VÀO MAP ĐỂ TEST
            this.rival.joinMap();

            // ❌ TẮT ACTIVE (không cho đánh)
            // this.rival.changeStatus(BossStatus.ACTIVE);
            // setup zone
            this.zone.isCompeting = true;
            this.zone.rank1 = clan.clanRank.rank;
            this.zone.rank2 = rivalClan.clanRank.rank;
            this.zone.rankName1 = clan.name;
            this.zone.rankName2 = rivalClan.name;

            // move clan
            clan.reloadClanMember();
            rivalClan.reloadClanMember();

            moveClan(clan);
            moveClan(rivalClan);

            // set pos
            Service.gI().setPos0(player, 334, 264);
            if (rival != null) {
                Service.gI().setPos0(rival, 434, 264);
            }

            // ❌ TẮT PK để test
            // PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.PK_PVP);
            // ===== TEST CHAT =====
            Service.gI().sendThongBao(player, "Rival Leader đã vào map (TEST)");
            Service.gI().chat(rival, "Ta là Leader Clan!");

            // ❌ TẮT countdown + thread
            // this.timeDown = 180;
            // this.isOpened = true;
            // new Thread(this, "Clan Rank").start();
        } catch (Exception e) {
            e.printStackTrace();
            dispose();
        }
    }

    public void startMatch() {
        new Thread(() -> {
            try {
                System.out.println("[ClanRank] >>> START MATCH (2 LEADER)");

                if (player == null || zone == null) {
                    return;
                }
                if (player.clan == null || rivalClan == null) {
                    return;
                }

                // ===== LẤY LEADER ĐỐI THỦ (FIX CHUẨN) =====
                ClanMember leaderMem = rivalClan.getLeader();

                if (leaderMem == null) {
                    Service.gI().sendThongBao(player, "Clan đối thủ không có leader!");
                    return;
                }

                Player rivalLeader = Client.gI().getPlayer(leaderMem.id);

                if (rivalLeader == null) {
                    Service.gI().sendThongBao(player, "Leader đối thủ không online!");
                    return;
                }

                // ===== TELEPORT =====
                ChangeMapService.gI().changeMap(player, zone, 300, 200);
                Thread.sleep(200);

                ChangeMapService.gI().changeMap(rivalLeader, zone, 400, 200);

                // ===== BẬT PK =====
                player.isPKClanRank = true;
                rivalLeader.isPKClanRank = true;

                PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.PK_PVP);
                PlayerService.gI().changeAndSendTypePK(rivalLeader, ConstPlayer.PK_PVP);

                // ===== THÔNG BÁO =====
                Service.gI().sendThongBao(player, "Đã vào trận với Leader đối thủ!");
                Service.gI().sendThongBao(rivalLeader, "Bạn đang bị thách đấu ClanRank!");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void moveClan(Clan clan) {
        for (ClanMember m : clan.members) {
            Player pl = Client.gI().getPlayer(m.id);
            if (pl != null && !pl.isDie()) {
                ChangeMapService.gI().changeMap(pl, zone, 100, 200);
            }
        }
    }

    @Override
    public void run() {
        // ❌ KHÔNG CHẠY LOOP KHI TEST
        /*
        while (!Maintenance.isRunning && isOpened) {
            try {
                update();
                Functions.sleep(1000);
            } catch (Exception e) {
                error++;
                if (error > 10) {
                    dispose();
                    return;
                }
            }
        }
         */
    }

    private void update() {
        if (player == null || zone == null) {
            dispose();
            return;
        }

        if (win) {
            return;
        }

        // ❌ TẮT COUNTDOWN
        /*
        if (timeUp < 5) {
            timeUp++;
            return;
        }
         */
        // ❌ TẮT LOGIC THẮNG THUA
        /*
        if (timeDown > 0) {
            timeDown--;

            if (player.isDie()) {
                lose();
                return;
            }

            if (rival == null || rival.isDie()) {
                win();
                return;
            }

        } else {
            lose();
        }
         */
    }

    public void win() {
        try {
            win = true;

            ClanRankDAO.loadDataClan(clan);
            ClanRankDAO.loadDataClan(rivalClan);

            clan.clanRank.win++;
            rivalClan.clanRank.lose++;

            clan.clanRank.rank = rankWin;
            rivalClan.clanRank.rank = rankLose;

            Service.gI().chat(player, ConstClanRank.TEXT_THANG);

        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
        dispose();
    }

    public void lose() {
        try {
            ClanRankDAO.loadDataClan(clan);
            ClanRankDAO.loadDataClan(rivalClan);

            clan.clanRank.lose++;
            rivalClan.clanRank.win++;

            clan.clanRank.rank = rankLose;
            rivalClan.clanRank.rank = rankWin;

            Service.gI().chat(player, ConstClanRank.TEXT_THUA);

        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
        dispose();
    }

    private void finish() {
        if (rival != null) {
            rival.leaveMap();
        }

        if (player != null) {
            PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.NON_PK);
        }
    }

    public void dispose() {
        isOpened = false;

        if (zone != null) {
            zone.isCompeting = false;
        }

        if (player != null) {
            player.isPKDHVT = false;
        }

        if (rival != null) {
            rival.leaveMap(); // 🔥 thêm để chắc chắn biến mất
            rival.dispose();
        }

        ClanRankManager.gI().removeSPR(this);

        zone = null;
        player = null;
        rival = null;
        clan = null;
        rivalClan = null;
    }
}
