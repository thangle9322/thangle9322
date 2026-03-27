package boss.boss_list_call.Broly;

/*
 * @Author: DienCoLamCoi
 * @Description: Điện Cơ Lâm Còi - Chuyên cung cấp thiết bị điện cơ uy tín chất lượng cao.
 * @Group Zalo: Giao lưu chia sẻ kinh nghiệm code - https://zalo.me/g/lsqfzx907
 */


import consts.BossStatus;
import static consts.BossType.BROLY;
import consts.ConstPlayer;
import boss.BossData;
import boss.BossID;
import boss.BossesData;
import boss.boss_list_call.Training.Boss_call;
import map.Zone;
import player.Player;
import skill.Skill;
import services.Service;
import services.SkillService;
import map.Service.ChangeMapService;
import utils.SkillUtil;
import utils.Util;

public class Broly extends Boss_call {
    
    public Broly(Player player) throws Exception {
        super(BROLY, BossID.BROLY, false, true, BossesData.BROLY,
                BossesData.SUPER_BROLY);

        this.playerAtt = player;
    }

     @Override
    public void joinMap() {
        //this.name = "Broly " + Util.nextInt(10, 100);
        this.name = this.data[this.currentLevel].getName() + " " + Util.nextInt(1, 100);
        this.nPoint.hpMax = Util.nextInt(100, 10000);
        this.nPoint.hp = this.nPoint.hpMax;
        this.nPoint.dame = this.nPoint.hpMax / 100;
        this.nPoint.crit = Util.nextInt(50);
        this.joinMap2(); //To change body of generated methods, choose Tools | Templates.
        st = System.currentTimeMillis();
    }
    
    public void joinMap2() {
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
            ChangeMapService.gI().changeMapYardrat(this, this.zone, x, y);
            this.changeStatus(BossStatus.CHAT_S);
            st = System.currentTimeMillis();
        }
     }

    private long st;
    

      @Override
    public void die(Player plKill) {
        this.changeStatus(BossStatus.DIE);
        if (plKill != null && plKill.isAdmin()) {             
            this.leaveMap();
            return;
        }
        if (this.nPoint.hpMax >= 5_000_000 && this.nPoint.hpMax <= 15_000_000) {
            this.leaveMap();
        }
    }
 
       @Override
    public void active() {
        super.active();
    }
    
     @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
         if (plAtt != null && plAtt.isAdmin()) {
             this.nPoint.setHp(0);
             this.setDie(plAtt);
             die(plAtt);
             return 0;
         }
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }
            if (Util.isTrue(1, 30)) {
                this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, 6));
                this.tangChiSo();
                SkillService.gI().useSkill(this, null, null, -1, null);
            }
            damage = this.nPoint.subDameInjureWithDeff(damage);
            if (!piercing && plAtt.playerSkill.skillSelect.template.id != Skill.TU_SAT && damage > this.nPoint.hpMax / 100) {
                damage = this.nPoint.hpMax / 100;
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
    
    private void tangChiSo() {
        int hpMax = this.nPoint.hpMax;
        int rand = Util.nextInt(80, 100);
        hpMax = hpMax + hpMax / rand < 16_070_777 ? hpMax + hpMax / rand : 16_070_777;
        this.nPoint.hpMax = hpMax;
        this.nPoint.dame = hpMax / 10;
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
                this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(7, this.playerSkill.skills.size() - 1));
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(5, 20)) {
                        if (SkillUtil.isUseSkillChuong(this)) {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 200)),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 70));
                        } else {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 40)),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 50));
                        }
                    }
                    if (Util.isTrue(1, 100)) {
                        this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, 6));
                        this.tangChiSo();
                    }

                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    checkPlayerDie(pl);
                } else {
                    if (Util.isTrue(1, 2)) {
                        this.moveToPlayer(pl);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
     
    @Override
    public void leaveMap() {
        Zone zone = this.zone;
        int x = this.location.x;
        int y = this.location.y;
        ChangeMapService.gI().exitMap(this);
        try {
            new SuperBroly(zone, x, y);
        } catch (Exception ex) {
        }
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
    }
    
}
