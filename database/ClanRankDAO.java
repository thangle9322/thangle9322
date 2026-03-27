package database;

import clan.Clan;
import clan.ClanMember;
import clan.ClanRankData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import matches.The23rdMartialArtCongress.ClanRankBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import player.Player;
import services.Service;
import utils.Logger;
import utils.TimeUtil;
import utils.Util;

public class ClanRankDAO {

    public static List<ClanRankBuilder> getPlayerListInRankRange(int rank, int limit) {
        Logger.logln("[ClanRankDAO] Lấy danh sách bang trong khoảng rank <= " + rank + ", giới hạn: " + limit);
        List<ClanRankBuilder> list = new ArrayList<>();
        try {
            DatabaseResultSet rs = DatabaseManager.executeQuery(
                "SELECT * FROM clan_rank WHERE rank <= ? AND rank > 0 ORDER BY rank DESC LIMIT ?",
                Math.max(rank, 10), limit
            );
            while (rs.next()) {
                list.add(readData(rs));
            }
            Logger.log("[ClanRankDAO] Lấy được " + list.size() + " bang từ truy vấn chính");
        } catch (Exception e) {
            Logger.log("[ClanRankDAO] Lỗi khi lấy danh sách bang theo khoảng rank" );
        }

        try {
            int rand = random(rank);
            Logger.log("[ClanRankDAO] Rank random thêm: " + rand);
            if (rand != -1) {
                DatabaseResultSet rs = DatabaseManager.executeQuery(
                    "SELECT * FROM clan_rank WHERE rank = ? LIMIT 1", rand
                );
                if (rs.first()) {
                    list.add(readData(rs));
                }
            }
        } catch (Exception e) {
            Logger.log("[ClanRankDAO] Lỗi khi thêm bang random vào danh sách" );
        }

        Collections.reverse(list);
        return list;
    }

    public static List<ClanRankBuilder> getPlayerListInRank(int rank, int limit) {
        Logger.log("[ClanRankDAO] Lấy danh sách bang theo rank, limit=" + limit);
        List<ClanRankBuilder> list = new ArrayList<>();
        try {
            DatabaseResultSet rs = DatabaseManager.executeQuery(
                "SELECT * FROM clan_rank WHERE rank > 0 ORDER BY rank ASC LIMIT ?", limit
            );
            while (rs.next()) {
                list.add(readData(rs));
            }
            Logger.log("[ClanRankDAO] Lấy được " + list.size() + " bang từ truy vấn chính");
        } catch (Exception e) {
            Logger.log("[ClanRankDAO] Lỗi khi lấy danh sách bang" );
        }

        try {
            if (rank > 100) {
                DatabaseResultSet rs = DatabaseManager.executeQuery(
                    "SELECT * FROM clan_rank WHERE rank > ? AND rank < ? ORDER BY rank ASC LIMIT 4",
                    rank - 3, rank + 2
                );
                while (rs.next()) {
                    list.add(readData(rs));
                }
                Logger.log("[ClanRankDAO] Đã thêm bang gần rank hiện tại vào danh sách");
            }
        } catch (Exception e) {
            Logger.log("[ClanRankDAO] Lỗi khi lấy bang gần rank hiện tại" );
        }

        return list;
    }

    public static int random(int rank) {
        if (rank > 10000) return Util.nextInt(6666, 10000);
        else if (rank > 6666) return Util.nextInt(3333, 6666);
        else if (rank > 3333) return Util.nextInt(1000, 3333);
        else if (rank > 1000) return Util.nextInt(666, 1000);
        else if (rank > 666) return Util.nextInt(333, 666);
        else if (rank > 333) return Util.nextInt(100, 333);
        return -1;
    }

    public static ClanRankBuilder readData(DatabaseResultSet rs) throws Exception {
        ClanRankBuilder builder = new ClanRankBuilder();

        if (rs != null) {
            builder.setClanid(rs.getInt("clan_id"));
            builder.setName(rs.getString("name"));
            builder.setRank(rs.getInt("rank"));
            builder.setLastPKTime(rs.getLong("last_pk_time"));
            builder.setLastTimeReward(rs.getLong("last_reward_time"));

            StringBuilder text = new StringBuilder();
            JSONParser parser = new JSONParser();

            try {
                JSONObject info = (JSONObject) parser.parse(rs.getString("info"));
                if (info != null) {
                    int head = info.containsKey("head") ? ((Long) info.get("head")).intValue() : 0;
                    int body = info.containsKey("body") ? ((Long) info.get("body")).intValue() : 0;
                    int leg = info.containsKey("leg") ? ((Long) info.get("leg")).intValue() : 0;

                    builder.setHead(head);
                    builder.setBody(body);
                    builder.setLeg(leg);

                    String leaderName = (String) info.get("leader_name");
                    text.append("Bang chủ: ").append(leaderName != null ? leaderName : "Không xác định");
                }
            } catch (Exception e) {
                Logger.log("[ClanRankDAO] Lỗi khi parse dữ liệu info của bang" );
                text.append("Bang chủ: Lỗi dữ liệu");
            }

            builder.setInfo(text.toString());
        }

        return builder;
    }

    public static void insertData(Player player) {
        if (player.clan == null) {
            Logger.log("[ClanRankDAO] Người chơi không thuộc bang, bỏ qua insert");
            return;
        }
        
        Clan clan = player.clan;

        JSONObject info = new JSONObject();
        info.put("head", player.getHead());
        info.put("body", player.getBody());
        info.put("leg", player.getLeg());
           
        ClanMember leader = clan.getLeader();
            info.put("leader_name", leader != null ? leader.name : "Không online");
 
        try {
            Logger.log("[ClanRankDAO] Thêm dữ liệu bang ID=" + clan.id + " vào bảng clan_rank");
            DatabaseManager.executeUpdate(
                "INSERT INTO clan_rank (clan_id, rank, name, info, last_pk_time, last_reward_time, win, lose, history) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                clan.id,
                clan.clanRank.rank,
                clan.name,
                info.toString(),
                clan.clanRank.lastPKTime,
                clan.clanRank.lastRewardTime,
                clan.clanRank.win,
                clan.clanRank.lose,
                "[]"
            );
        } catch (Exception e) {
            Logger.log("[ClanRankDAO] Lỗi khi insert dữ liệu bang");
        }
    }

    public static void updateData(Player player) {
        if (player.clan == null) {
            Logger.log("[ClanRankDAO] Người chơi không thuộc bang, bỏ qua update");
            return;
        }

        JSONObject info = new JSONObject();
        info.put("head", player.getHead());
        info.put("body", player.getBody());
        info.put("leg", player.getLeg());
        Clan clan = player.clan;
           
        ClanMember leader = clan.getLeader();
            info.put("leader_name", leader != null ? leader.name : "Không online");
 
        try {
            Logger.log("[ClanRankDAO] Cập nhật dữ liệu bang ID=" + clan.id);
            DatabaseManager.executeUpdate(
                "UPDATE clan_rank SET rank = ?, name = ?, info = ?, last_pk_time = ?, last_reward_time = ?, win = ?, lose = ?, history = ? WHERE clan_id = ?",
                clan.clanRank.rank,
                clan.name,
                info.toString(),
                clan.clanRank.lastPKTime,
                clan.clanRank.lastRewardTime,
                clan.clanRank.win,
                clan.clanRank.lose,
                "[]",
                clan.id
            );
        } catch (Exception e) {
            Logger.log("[ClanRankDAO] Lỗi khi update dữ liệu bang" );
        }
    }

    public static void loadData(Player player) {
        if (player.clan == null) {
            Logger.log("[ClanRankDAO] Người chơi không thuộc bang, không thể load dữ liệu");
            Service.gI().sendThongBao(player, "Có lỗi xảy ra");
            return;
        }
        try {
            Clan clan = player.clan;
            Logger.log("[ClanRankDAO] Load dữ liệu bang ID=" + clan.id);
            DatabaseResultSet rs = DatabaseManager.executeQuery("SELECT * FROM clan_rank WHERE clan_id = " + clan.id);
            if (rs.first()) {
                clan.clanRank.rank = rs.getInt("rank");
                clan.clanRank.lastPKTime = rs.getLong("last_pk_time");
                clan.clanRank.lastRewardTime = rs.getLong("last_reward_time");
                clan.clanRank.win = rs.getInt("win");
                clan.clanRank.lose = rs.getInt("lose");
            }
        } catch (Exception e) {
            Logger.log("[ClanRankDAO] Lỗi khi load dữ liệu bang");
        }
    }

    public static int getRank(int clanId) {
        try {
            Logger.log("[ClanRankDAO] Lấy rank của bang ID=" + clanId);
            DatabaseResultSet rs = DatabaseManager.executeQuery("SELECT rank FROM clan_rank WHERE clan_id = " + clanId);
            if (rs.first()) {
                return rs.getInt("rank");
            }
        } catch (Exception e) {
            Logger.log("[ClanRankDAO] Lỗi khi lấy rank bang" );
        }
        return getCurrentHighestRank() + 1;
    }

    public static int getCurrentHighestRank() {
        try {
            DatabaseResultSet rs = DatabaseManager.executeQuery("SELECT rank FROM clan_rank ORDER BY rank DESC LIMIT 1");
            if (rs.first()) {
                return rs.getInt("rank");
            }
        } catch (Exception e) {
            Logger.log("[ClanRankDAO] Lỗi khi lấy rank cao nhất" );
        }
        return 0;
    }
    
    public static void loadDataClan(Clan clan) {
    try {
        if (clan == null) return;

        // Nếu đã có rank rồi thì không cần load lại
        if (clan.clanRank != null && clan.clanRank.rank > 0) {
            return;
        }

        String sql = "SELECT rank FROM clan_rank WHERE clan_id = ?";
        DatabaseResultSet rs = DatabaseManager.executeQuery(sql, clan.id); // sửa theo DB bạn

        if (rs.next()) {
            if (clan.clanRank == null) {
                clan.clanRank = new ClanRankData(clan); // ⚠️ sửa đúng class bạn đang dùng
            }
            clan.clanRank.rank = rs.getInt("rank");

            Logger.primaryln("[ClanRankDAO] Load rank clan " + clan.id + " = " + clan.clanRank.rank);

        } else {
            // Không có dữ liệu
            if (clan.clanRank == null) {
                clan.clanRank = new ClanRankData(clan);
            }
            clan.clanRank.rank = -1;

            Logger.primaryln("[ClanRankDAO] Không có rank clan_id=" + clan.id);
        }

        rs.dispose();

    } catch (Exception e) {
        e.printStackTrace();
    }
}
    
     public static void deleteRank(int clanId) {
        try {
            Logger.log("[ClanRankDAO] Xóa clan ID=" + clanId + " kh?i bang clan_rank");
            DatabaseManager.executeUpdate(
                    "DELETE FROM clan_rank WHERE clan_id = ?",
                    clanId
            );
        } catch (Exception e) {
            Logger.log("[ClanRankDAO] L?i xóa clan kh?i rank: " + e.getMessage());
        }
    }
     
}
