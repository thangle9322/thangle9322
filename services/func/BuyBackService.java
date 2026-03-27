package services.func;
import item.Item;
import player.Player;
import player.Service.InventoryService;
import services.ItemService;
import services.Service;
import shop.ShopService;

public class BuyBackService {

    private static final byte MAX_ITEM_IN_BOX = 10;

    private static BuyBackService i;

    public static BuyBackService gI() {
        if (i == null) {
            i = new BuyBackService();
        }
        return i;
    }

    public void addItem(Player player, Item item) {
        if (player.inventory.itemsDaBan.size() + 1 > MAX_ITEM_IN_BOX) {
            player.inventory.itemsDaBan.remove(0);
        }
        Item itemmua = ItemService.gI().copyItem(item);
        player.inventory.itemsDaBan.add(itemmua);
        if (player.idMark != null && player.idMark.getTagNameShop().equals("ITEMS_DABAN")) {
            ShopService.gI().opendShop(player, "ITEMS_DABAN", true);
        }
    }
    public Item getBuyBack(Player seller) {
        for (Item item : seller.inventory.itemsBody) {
            if (item != null && item.isNotNullItem() && item.template.type == 5) {
                return ItemService.gI().copyItem(item);
            }
        }
        return null;
    }

public void buyCostume(Player buyer, Player seller) {
    Item costume = getBuyBack(seller);
    if (costume == null) {
        Service.gI().sendThongBao(buyer, "Người bán không có cải trang!");
        return;
    }
    
    // ✅ Lấy giá từ item
    int cost = costume.template.gem > 0 ? costume.template.gem : ShopService.gI().getItemGemCost(costume.template.id);
    
    // ✅ NẾU GIÁU = 0 → KHÔNG MUA ĐƯỢC
    if (cost <= 0) {
        Service.gI().sendThongBao(buyer, "Cải trang này không thể bán!");
        return;
    }

    int priceBuy = (int) (cost * 0.8);
    int reward = (int) (cost * 0.6);

    if (buyer.inventory.gem < priceBuy) {
        Service.gI().sendThongBao(buyer, "Bạn không đủ ngọc để mua!");
        return;
    }

    // Trừ buyer
    buyer.inventory.gem -= priceBuy;
    Service.gI().sendMoney(buyer);

    // Cộng seller
    seller.inventory.gem += reward;
    Service.gI().sendMoney(seller);

    // Add cải trang cho buyer
    Item newItem = ItemService.gI().copyItem(costume);
    InventoryService.gI().addItemBag(buyer, newItem);
    InventoryService.gI().sendItemBags(buyer);

    // Thông báo
    Service.gI().sendThongBao(buyer, "Mua " + costume.template.name + " thành công!");
    Service.gI().sendThongBao(seller, "Bán " + costume.template.name + " cho " + buyer.name);
}

}
