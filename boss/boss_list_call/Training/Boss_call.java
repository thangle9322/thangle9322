package boss.boss_list_call.Training;

/*
 * @Author: DienCoLamCoi
 * @Description: Điện Cơ Lâm Còi - Chuyên cung cấp thiết bị điện cơ uy tín chất lượng cao.
 * @Group Zalo: Giao lưu chia sẻ kinh nghiệm code - https://zalo.me/g/lsqfzx907
 */


import consts.ConstRatio;
import boss.Boss;
import boss.BossData;
import consts.BossStatus;
import consts.BossType;
 

import consts.ConstPlayer;
import java.io.IOException;
import map.Service.ChangeMapService;
import map.Service.MapService;
 import player.Player;
import player.Service.PlayerService;
import server.ServerNotify;
import services.EffectSkillService;
 import services.Service;
import services.SkillService;
import services.TaskService;
 import utils.Logger;
import utils.SkillUtil;
import utils.Util;

public abstract class Boss_call extends Boss {

    public Player playerAtt;
    protected long timeJoinMap;
    protected long lastTimeAFK;
    protected long lastTimeMove;
    public boolean doneChatS;
    public long lastTimeChat;
    protected boolean isPlayerDie;
    public long lastTimeBuff;
    public int bosscount = 0;
    public boolean isboss;

    public Boss_call(BossType ducvupro, int id, boolean isNotifyDisabled, boolean isZone01SpawnDisabled, BossData... data) throws Exception {
        super(ducvupro, id, data);
        this.isNotifyDisabled = isNotifyDisabled;
        this.isZone01SpawnDisabled = isZone01SpawnDisabled;
        this.bossStatus = BossStatus.RESPAWN;
    }
    
    public Boss_call(int id, boolean isNotifyDisabled, boolean isZone01SpawnDisabled, BossData... data) throws Exception {
        super(id, data);
        this.isNotifyDisabled = isNotifyDisabled;
        this.isZone01SpawnDisabled = isZone01SpawnDisabled;
    }

    @Override
    public void checkPlayerDie(Player pl) {
        if (pl.isDie()) {
            
        }
    }

    @Override
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }
        this.attack();
    }

    
    protected void goToXY(int x, int y, boolean isTeleport) {
        if (!isTeleport) {
            byte dir = (byte) (this.location.x - x < 0 ? 1 : -1);
            byte move = (byte) Util.nextInt(50, 100);
            PlayerService.gI().playerMove(this, this.location.x + (dir == 1 ? move : -move), y);
        } else {
            ChangeMapService.gI().changeMapYardrat(this, this.zone, x, y);
        }
    }

    protected void goToXY(int x, int y) {
        byte dir = (byte) (this.location.x - x < 0 ? 1 : -1);
        byte move = (byte) Util.nextInt(50, 100);
        this.location.x = this.location.x + (dir == 1 ? move : -move);
        this.location.y = y;
        MapService.gI().sendPlayerMove(this);
    }

    @Override
    public void attack() {
        try {
            if (playerAtt.location != null && playerAtt != null && playerAtt.zone != null && this.zone != null && this.zone.equals(playerAtt.zone)) {
                if (this.isDie()) {
                    return;
                }
              
                this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
                if (Util.getDistance(this, playerAtt) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(15, ConstRatio.PER100) && SkillUtil.isUseSkillChuong(this)) {
                        goToXY(playerAtt.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 80)), Util.nextInt(10) % 2 == 0 ? playerAtt.location.y : playerAtt.location.y - Util.nextInt(0, 50), false);
                    }
                    SkillService.gI().useSkill(this, playerAtt, null, -1, null);
                    checkPlayerDie(playerAtt);
                } else {
                    if (Util.isTrue(1, 2)) {
                        this.moveToPlayer(playerAtt);
                    }
                }
            } else {
                this.leaveMap();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
 

    @Override
    public void joinMap() {
        if (playerAtt.zone != null) {
            this.zone = playerAtt.zone;
            int x = this.zone.map.mapWidth > 100 ? Util.nextInt(100, this.zone.map.mapWidth - 100) : Util.nextInt(100);
            int y = this.zone.map.yPhysicInTop(x, 100);
            ChangeMapService.gI().changeMap(this, this.zone, x, y);
            this.changeStatus(BossStatus.CHAT_S);
        }
    }

 
    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(400, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }

            if (plAtt != null && plAtt.idNRNM != -1) {
                return 1;
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

    protected void notifyPlayeKill(Player player) {

    }

   
    
    @Override
    public void leaveMap() {
        if (this.currentLevel < this.data.length - 1) {
            this.lastZone = this.zone;
            this.changeStatus(BossStatus.RESPAWN);
        } else {
            ChangeMapService.gI().exitMap(this);
            this.lastZone = null;
            this.lastTimeRest = System.currentTimeMillis();
            this.changeStatus(BossStatus.REST);
        }
        this.wakeupAnotherBossWhenDisappear();
    }

  
}
