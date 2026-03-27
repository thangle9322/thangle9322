package boss.boss_list_call.Nappa;

/*
 * @Author: DienCoLamCoi
 * @Description: Điện Cơ Lâm Còi - Chuyên cung cấp thiết bị điện cơ uy tín chất lượng cao.
 * @Group Zalo: Giao lưu chia sẻ kinh nghiệm code - https://zalo.me/g/lsqfzx907
 */


import boss.Boss;
import boss.BossID;
 
import consts.BossStatus;
import consts.ConstPlayer;
import consts.ConstTask;
import boss.BossesData;
import boss.boss_list_call.Training.Boss_call;
import map.Service.ChangeMapService;
import player.Player;
import services.EffectSkillService;
import services.Service;
import services.SkillService;
import services.TaskService;

import utils.Util;

public class Kuku extends Boss_call {

    private long st;

    public Kuku(Player player) throws Exception {
        super(BossID.KUKU, true, true, BossesData.KUKU);
        this.playerAtt = player;
    }

    @Override
    public void joinMap() {
          if (playerAtt.zone == null ) { 
        Service.gI().sendThongBao(playerAtt, "Không thể thực thi lệnh  ");
        this.changeStatus(BossStatus.RESPAWN);
        return;
    }
        if (playerAtt.zone != null) {
            int x = playerAtt.location.x ;
            int y = playerAtt.location.y;
            this.zone = playerAtt.zone;
            this.location.x = x;
            this.location.y = y;
            if (this.zone.getBosses() != null && this.zone.getBosses().size() > 5) { 
            Service.gI().sendThongBao(playerAtt, "Giới hạn tối đa 5 boss trong khu vực");
            this.changeStatus(BossStatus.REST);
            return;
        }
            ChangeMapService.gI().changeMap(this, this.zone, x, y);
            this.changeStatus(BossStatus.CHAT_S);
            st = System.currentTimeMillis();
        }
    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, 900000)) {
            this.changeStatus(BossStatus.LEAVE_MAP);
        }
    }
    
    @Override
    public void active() {
        super.active();
    }
    
     @Override
    public void reward(Player plKill) {
        if (TaskService.gI().getIdTask(plKill) == ConstTask.TASK_19_0) {
          TaskService.gI().checkDoneTaskKillBoss(playerAtt, this);
            return;
        }
    }
    
    
    @Override
     public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon - plAtt.nPoint.tlchinhxac, 1)) {
                this.chat("Xí hụt");
                return 0;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage);
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }
            this.nPoint.subHP(damage);
            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }
            return (int) damage;
        } else {
            return 0;
        }
    }
         @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100) && this.typePk == ConstPlayer.PK_ALL) {
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.isDie()) {
                    return;
                }
                this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                int dis = Util.getDistance(this, pl);
                if (dis > 450) {
                    move(pl.location.x - 24, pl.location.y);
                } else if (dis > 100) {
                    int dir = (this.location.x - pl.location.x < 0 ? 1 : -1);
                    int move = Util.nextInt(50, 100);
                    move(this.location.x + (dir == 1 ? move : -move), pl.location.y);
                } else {
                    if (Util.isTrue(30, 100)) {
                        int move = Util.nextInt(50);
                        move(pl.location.x + (Util.nextInt(0, 1) == 1 ? move : -move), this.location.y);
                    }
                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    checkPlayerDie(pl);
                }
                }else{
                      if (Util.isTrue(1, 2)) {
                        this.moveToPlayer(pl);
                      }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
