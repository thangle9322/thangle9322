package boss.boss_list_call.Broly;

/*
 * @Author: DienCoLamCoi
 * @Description: Điện Cơ Lâm Còi - Chuyên cung cấp thiết bị điện cơ uy tín chất lượng cao.
 * @Group Zalo: Giao lưu chia sẻ kinh nghiệm code - https://zalo.me/g/lsqfzx907
 */


import boss.Boss;
 
import boss.BossData;
import boss.BossID;
import boss.BossManager.BrolyManager;
import boss.BossesData;
 
import consts.BossStatus;
import static consts.BossType.BROLY;
import consts.ConstPlayer;
import boss.boss_list_call.Training.Boss_call;
import map.Service.ChangeMapService;
import map.Zone;
import player.Player;
import services.PetService;
import services.SkillService;
import skill.Skill;
import utils.SkillUtil;
import utils.Util;

public class SuperBroly extends Boss_call {

    public SuperBroly(Zone zone, int x, int y) throws Exception {

        super(BROLY, BossID.SUPER_BROLY, false, true, BossesData.SUPER_BROLY
        );
        this.zone = zone;
        this.location.x = x;
        this.location.y = y;
    }

    @Override
    public void reward(Player plKill) {
        if (plKill.pet == null) {
            PetService.gI().createNormalPet(plKill);
        }
    }

    @Override
    public void active() {
        super.active();
    }

    @Override
    public void joinMap() {
       // this.name = "Super Broly " + Util.nextInt(10, 100);
        this.name = this.data[this.currentLevel].getName() + " " + Util.nextInt(1, 100);
        this.nPoint.hpMax = Util.nextInt(1_000_000, 16_070_777);
        this.nPoint.hp = this.nPoint.hpMax;
        this.nPoint.dame = this.nPoint.hpMax / 100;
        this.nPoint.crit = Util.nextInt(50);
        if (Util.isTrue(3, 5) && this.zone != null) {
            ChangeMapService.gI().changeMap(this, this.zone, this.location.x, this.location.y);
            this.changeStatus(BossStatus.CHAT_S);
            this.notifyJoinMap();
        } else {
            super.joinMap();
        }
        PetService.gI().createNormalPet(this);
//        PlayerService.gI().changeAndSendTypePK(this.pet, ConstPlayer.PK_ALL);
        st = System.currentTimeMillis();
    }

    private long st;

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, 900000)) {
            this.leaveMap();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
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

    private long lastTimeAttack;

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

    private void tangChiSo() {
        int hpMax = this.nPoint.hpMax;
        int rand = Util.nextInt(80, 100);
        hpMax = hpMax + hpMax / rand < 16_070_777 ? hpMax + hpMax / rand : 16_070_777;
        this.nPoint.hpMax = hpMax;
        this.nPoint.dame = hpMax / 10;
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        if (this.pet != null) {
            ChangeMapService.gI().exitMap(this.pet);
        }
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
        BrolyManager.gI().removeBoss(this);
        this.dispose();
    }
}
