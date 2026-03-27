package shop;


import java.util.ArrayList;
import player.Player;
import skill.Skill;
import utils.SkillUtil;

public class TabShopHocKynang extends TabShop {

    public TabShopHocKynang(TabShop tabShop, Player player) {
        this.itemShops = new ArrayList<>();
        this.shop = tabShop.shop;
        this.id = tabShop.id;
        this.name = tabShop.name;

        for (ItemShop itemShop : tabShop.itemShops) {
            if (itemShop.temp.gender == player.gender || itemShop.temp.gender > 2) {
                boolean shouldAdd = true;
                for (Integer i : player.BoughtSkill) {//check xem co ki nang chua
                    if (itemShop.temp.id == i) {
                        shouldAdd = false;
                        break;
                    }
                }
                Skill currentSkill = SkillUtil.getSkillByItemID(player, itemShop.temp.id);
                byte skillLevelInShop = itemShop.getLevelSkill();
                if (currentSkill != null && currentSkill.point >= skillLevelInShop) {
                    shouldAdd = false;
                }
                if (shouldAdd) {
                    
                    this.itemShops.add(new ItemShop(itemShop));
                }
            }
        }
    }
}
