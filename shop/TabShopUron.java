package shop;

import player.Player;
import java.util.ArrayList;
import java.util.List;
import player.Service.InventoryService;

public class TabShopUron extends TabShop {

    private final int[] listDauThan = {293, 294, 295, 296, 297, 298, 299, 596, 597, 598};

    public TabShopUron(TabShop tabShop, Player player) {
        this.itemShops = new ArrayList<>();
        this.shop = tabShop.shop;
        this.id = tabShop.id;
        this.name = tabShop.name;

        int dauCanBuyId = idDauCanBuy(player);
        boolean hasBongTai = InventoryService.gI().findItemBongTai(player);
        
        for (ItemShop itemShop : tabShop.itemShops) {
            int itemId = itemShop.temp.id;
            String itemName = itemShop.temp.name;
         
            // Kiểm tra giới tính
            if (itemShop.temp.gender != player.gender && itemShop.temp.gender <= 2) {
                 
                continue;
            }

            // Kiểm tra đã sở hữu
            if (InventoryService.gI().hasItem(player, itemId)) {               
                continue;
            }

            // Bỏ qua Tennis Space Ship
            if (player.haveTennisSpaceShip && itemId == 453) {
                 
                continue;
            }

            // Bỏ qua bông tai nếu đã có
            if (hasBongTai && itemId == 454) {
                 
                continue;
            }

            // Kiểm tra đậu thần
            boolean isInListDauThan = false;
            for (int id : listDauThan) {
                if (itemId == id) {
                    isInListDauThan = true;
                    break;
                }
            }

            if (isInListDauThan && itemId != dauCanBuyId) {
                 
                continue;
            }
            // Nếu qua hết thì thêm vào shop
            this.itemShops.add(new ItemShop(itemShop));         
        }
         
    }

    public int idDauCanBuy(Player player) {
        int level = player.magicTree.level;
        if (level == 10) {
            return listDauThan[9];
        } else if (level >= 1 && level <= 9) {
            return listDauThan[level];
        }
        throw new IllegalArgumentException("Invalid magic tree level: " + level);
    }
}
