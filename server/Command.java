package server;

import consts.ConstNpc;
import managers.GiftCodeManager;
import item.Item;
import player.Pet;
import player.Player;
import network.SessionManager;
import services.ItemService;
import services.PetService;
import services.Service;
import services.func.Input;
import map.Service.ChangeMapService;
import map.Service.NpcService;
import player.Service.InventoryService;
import utils.SystemMetrics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import services.SkillService;
import services.TaskService;
import skill.Skill;

public class Command {

    private static Command instance;

    private final Map<String, Consumer<Player>> adminCommands = new HashMap<>();
    private final Map<String, BiConsumer<Player, String>> parameterizedCommands = new HashMap<>();

    public static Command gI() {
        if (instance == null) {
            instance = new Command();
        }
        return instance;
    }

    private Command() {
        initAdminCommands();
        initParameterizedCommands();
    }

    private void initAdminCommands() {
        adminCommands.put("item", player -> Input.gI().createFormGiveItem(player));
        adminCommands.put("getitem", player -> Input.gI().createFormGetItem(player));
        adminCommands.put("hoiskill", player -> Service.gI().releaseCooldownSkill(player));
        adminCommands.put("d", player -> Service.gI().setPos(player, player.location.x, player.location.y + 10));
        adminCommands.put("menu", player -> NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_ADMIN, -1,
                "|0|Time start: " + ServerManager.timeStart 
                + "\nClients: " + Client.gI().getPlayers().size()
                + " người chơi\n Sessions: " + SessionManager.gI().getNumSession() 
                + "\nThreads: " + Thread.activeCount()
                + " luồng" + "\n" + SystemMetrics.ToString(),
                "Ngọc rồng", "Đệ tử", "Bảo trì", "Tìm kiếm\nngười chơi", "Boss", "Đóng"));
    }

    private void initParameterizedCommands() {
        parameterizedCommands.put("m ", (player, text) -> {
            int mapId = Integer.parseInt(text.replace("m ", ""));
            ChangeMapService.gI().changeMapInYard(player, mapId, -1, -1);
        });

        parameterizedCommands.put("toado", (player, text) -> {
            Service.gI().sendThongBaoOK(player, "x: " + player.location.x + " - y: " + player.location.y);
        });
        
        parameterizedCommands.put("battu", (player, text) -> {
        player.batTu = !player.batTu;
        Service.gI().sendThongBao(player, player.batTu ? "Bất tử" : "Tắt bất tử");
        });
        
        parameterizedCommands.put("test", (player, text) -> {

                switch (player.gender) {
                    case 0 ->{
                        SkillService.gI().learSkillSpecial(player, Skill.SUPER_KAME, 1);
                    }
                    case 2 ->{
                        SkillService.gI().learSkillSpecial(player, Skill.LIEN_HOAN_CHUONG, 1);
                    }
                    default ->
                        SkillService.gI().learSkillSpecial(player, Skill.MA_PHONG_BA, 1);
                }
                });
        parameterizedCommands.put("n", (player, text) -> {
            int idTask = Integer.parseInt(text.replaceAll("n", ""));
            player.playerTask.taskMain.id = idTask - 1;
            player.playerTask.taskMain.index = 0;
            TaskService.gI().sendNextTaskMain(player);
        });
        parameterizedCommands.put("i ", (player, text) -> {
            int itemId = Integer.parseInt(text.replace("i ", ""));
            Item item = ItemService.gI().createNewItem(((short) itemId));
            List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop((short) itemId);
            if (!ops.isEmpty()) {
                item.itemOptions = ops;
            }
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "GET " + item.template.name + " [" + item.template.id + "] SUCCESS !");
        });

        parameterizedCommands.put("playerhp", (player, text) -> {
            long val = Long.parseLong(text.replace("playerhp", "").trim());
            player.nPoint.hpg =(int)val;
            Service.gI().point(player);
            Service.gI().sendThongBao(player, "Đã buff HP = " + val);
        });

        parameterizedCommands.put("playermp", (player, text) -> {
            long val = Long.parseLong(text.replace("playermp", "").trim());
            player.nPoint.mpg =(int)val;
            Service.gI().point(player);
            Service.gI().sendThongBao(player, "Đã buff KI = " + val);
        });

        parameterizedCommands.put("playerdmg", (player, text) -> {
            long val = Long.parseLong(text.replace("playerdmg", "").trim());
            player.nPoint.dameg =(int)val;
            Service.gI().point(player);
            Service.gI().sendThongBao(player, "Đã buff Sát thương = " + val);
        });

        parameterizedCommands.put("playerdef", (player, text) -> {
            long val = Long.parseLong(text.replace("playerdef", "").trim());
            player.nPoint.defg =(int)val;
            Service.gI().point(player);
            Service.gI().sendThongBao(player, "Đã buff Giáp = " + val);
        });

        parameterizedCommands.put("playercrit", (player, text) -> {
            long val = Long.parseLong(text.replace("playercrit", "").trim());
            player.nPoint.critg =(int)val;
            Service.gI().point(player);
            Service.gI().sendThongBao(player, "Đã buff Chí mạng = " + val);
        });

        parameterizedCommands.put("playerpw", (player, text) -> {
            long val = Long.parseLong(text.replace("playerpw", "").trim());
            player.nPoint.power = val;
            Service.gI().point(player);
            Service.gI().sendThongBao(player, "Đã buff Sức mạnh = " + val);
        });

        parameterizedCommands.put("playertn", (player, text) -> {
            long val = Long.parseLong(text.replace("playertn", "").trim());
            player.nPoint.tiemNang = val;
            Service.gI().point(player);
            Service.gI().sendThongBao(player, "Đã buff Tiềm năng = " + val);
        });

        parameterizedCommands.put("playerli", (player, text) -> {
            byte val = Byte.parseByte(text.replace("playerli", "").trim());
            player.nPoint.limitPower = val;
            Service.gI().point(player);
            Service.gI().sendThongBao(player, "Đã buff Giới hạn sức mạnh = " + val);
        });

        parameterizedCommands.put("pethp", (player, text) -> {
            if (player.pet == null) {
                Service.gI().sendThongBao(player, "Bạn chưa có đệ tử!");
                return;
            }
            long val = Long.parseLong(text.replace("pethp", "").trim());
            player.pet.nPoint.hpg =(int)val;
            Service.gI().point(player);
            Service.gI().sendThongBao(player, "Đã buff HP pet = " + val);
        });

        parameterizedCommands.put("petmp", (player, text) -> {
            if (player.pet == null) {
                Service.gI().sendThongBao(player, "Bạn chưa có đệ tử!");
                return;
            }
            long val = Long.parseLong(text.replace("petmp", "").trim());
            player.pet.nPoint.mpg =(int)val;
            Service.gI().point(player);
            Service.gI().sendThongBao(player, "Đã buff KI pet = " + val);
        });

        parameterizedCommands.put("petdmg", (player, text) -> {
            if (player.pet == null) {
                Service.gI().sendThongBao(player, "Bạn chưa có đệ tử!");
                return;
            }
            long val = Long.parseLong(text.replace("petdmg", "").trim());
            player.pet.nPoint.dameg =(int)val;
            Service.gI().point(player);
            Service.gI().sendThongBao(player, "Đã buff Sát thương pet = " + val);
        });

        parameterizedCommands.put("petdef", (player, text) -> {
            if (player.pet == null) {
                Service.gI().sendThongBao(player, "Bạn chưa có đệ tử!");
                return;
            }
            long val = Long.parseLong(text.replace("petdef", "").trim());
            player.pet.nPoint.defg =(int)val;
            Service.gI().point(player);
            Service.gI().sendThongBao(player, "Đã buff Giáp pet = " + val);
        });

        parameterizedCommands.put("petcrit", (player, text) -> {
            if (player.pet == null) {
                Service.gI().sendThongBao(player, "Bạn chưa có đệ tử!");
                return;
            }
            long val = Long.parseLong(text.replace("petcrit", "").trim());
            player.pet.nPoint.critg = (int)val;
            Service.gI().point(player);
            Service.gI().sendThongBao(player, "Đã buff Chí mạng pet = " + val);
        });

        parameterizedCommands.put("petpw", (player, text) -> {
            if (player.pet == null) {
                Service.gI().sendThongBao(player, "Bạn chưa có đệ tử!");
                return;
            }
            long val = Long.parseLong(text.replace("petpw", "").trim());
            player.pet.nPoint.power = val;
            Service.gI().point(player);
            Service.gI().sendThongBao(player, "Đã buff Sức mạnh pet = " + val);
        });

        parameterizedCommands.put("pettn", (player, text) -> {
            if (player.pet == null) {
                Service.gI().sendThongBao(player, "Bạn chưa có đệ tử!");
                return;
            }
            long val = Long.parseLong(text.replace("pettn", "").trim());
            player.pet.nPoint.tiemNang = val;
            Service.gI().point(player);
            Service.gI().sendThongBao(player, "Đã buff Tiềm năng pet = " + val);
        });

        parameterizedCommands.put("petli", (player, text) -> {
            if (player.pet == null) {
                Service.gI().sendThongBao(player, "Bạn chưa có đệ tử!");
                return;
            }
            byte val = Byte.parseByte(text.replace("petli", "").trim());
            player.pet.nPoint.limitPower = val;
            Service.gI().point(player);
            Service.gI().sendThongBao(player, "Đã buff Giới hạn sức mạnh pet = " + val);
        });

        parameterizedCommands.put("playerhn", (player, text) -> {
            long val = Long.parseLong(text.replace("playerhn", "").trim());
            player.inventory.ruby = (int)val;
            Service.gI().sendMoney(player);
            Service.gI().sendThongBao(player, "Đã buff Hồng Ngọc = " + val);
        });

        parameterizedCommands.put("playergold", (player, text) -> {
            long val = Long.parseLong(text.replace("playergold", "").trim());
            player.inventory.gold = val;
            Service.gI().sendMoney(player);
            Service.gI().sendThongBao(player, "Đã buff Vàng = " + val);
        });
        parameterizedCommands.put("xoa", (player, text) -> {
            String[] parts = text.trim().split("\\s+");
            if (parts.length < 2) {
                Service.gI().sendThongBao(player, "Cú pháp: xoa [0=body|1=bag|2=box|3=all]");
                return;
            }

            int type;
            try {
                type = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                Service.gI().sendThongBao(player, "Loại không hợp lệ!");
                return;
            }

            switch (type) {
                case 0 -> { // xoá đồ trên người
                    for (int i = 0; i < player.inventory.itemsBody.size(); i++) {
                        InventoryService.gI().removeItemBody(player, i);
                    }
                    Service.gI().sendThongBao(player, "Đã xoá hết đồ Body!");
                }
                case 1 -> { // xoá đồ trong túi
                    for (int i = 0; i < player.inventory.itemsBag.size(); i++) {
                        InventoryService.gI().removeItemBag(player, i);
                    }
                    Service.gI().sendThongBao(player, "Đã xoá hết đồ Bag!");
                }
                case 2 -> { // xoá đồ trong rương
                    for (int i = 0; i < player.inventory.itemsBox.size(); i++) {
                        InventoryService.gI().removeItemBox(player, i);
                    }
                    Service.gI().sendThongBao(player, "Đã xoá hết đồ Box!");
                }
                case 3 -> { // xoá tất cả
                    for (int i = 0; i < player.inventory.itemsBody.size(); i++) {
                        InventoryService.gI().removeItemBody(player, i);
                    }
                    for (int i = 0; i < player.inventory.itemsBag.size(); i++) {
                        InventoryService.gI().removeItemBag(player, i);
                    }
                    for (int i = 0; i < player.inventory.itemsBox.size(); i++) {
                        InventoryService.gI().removeItemBox(player, i);
                    }
                    Service.gI().sendThongBao(player, "Đã xoá toàn bộ Body, Bag, Box!");
                }
                default ->
                    Service.gI().sendThongBao(player, "Loại không hợp lệ!");
            }

            // cập nhật lại inventory cho client
            InventoryService.gI().sendItemBody(player);
            InventoryService.gI().sendItemBags(player);
            InventoryService.gI().sendItemBox(player);
        });

        parameterizedCommands.put("captcha", (player, text) -> {
            String[] parts = text.trim().split("\\s+");
            if (parts.length > 1) {
                switch (parts[1].toLowerCase()) {
                    case "on" -> {
                        Manager.ENABLE_CAPTCHA = true;
                        Manager.properties.setProperty("enableCaptcha", "true");
                        Service.saveProperties();
                        Service.gI().sendThongBao(player, "Captcha đã bật");
                    }
                    case "off" -> {
                        Manager.ENABLE_CAPTCHA = false;
                        Manager.properties.setProperty("enableCaptcha", "false");
                        Service.saveProperties();
                        Service.gI().sendThongBao(player, "Captcha đã tắt");
                    }
                    default ->
                        Service.gI().sendThongBao(player, "Cú pháp: captcha [on|off]");
                }
                return;
            }
            // Không có tham số → báo trạng thái hiện tại
            Service.gI().sendThongBao(player,
                    Manager.ENABLE_CAPTCHA ? "Captcha hiện đang bật" : "Captcha hiện đang tắt");
        });

    }

    public void chat(Player player, String text) {
        if (!check(player, text)) {
            Service.gI().chat(player, text);
        }
    }

    public boolean check(Player player, String text) {
        if (player.isAdmin()) {
            if (adminCommands.containsKey(text)) {
                adminCommands.get(text).accept(player);
                return true;
            }

            for (Map.Entry<String, BiConsumer<Player, String>> entry : parameterizedCommands.entrySet()) {
                if (text.startsWith(entry.getKey())) {
                    entry.getValue().accept(player, text);
                    return true;
                }
            }
        }

        if (text.startsWith("ten con la ")) {
            PetService.gI().changeNamePet(player, text.replaceAll("ten con la ", ""));
        }

        if (player.pet != null) {
            switch (text) {
                case "di theo", "follow" ->
                    player.pet.changeStatus(Pet.FOLLOW);
                case "bao ve", "protect" ->
                    player.pet.changeStatus(Pet.PROTECT);
                case "tan cong", "attack" ->
                    player.pet.changeStatus(Pet.ATTACK);
                case "ve nha", "go home" ->
                    player.pet.changeStatus(Pet.GOHOME);
                case "bien hinh" ->
                    player.pet.transform();
            }
        }
        return false;
    }
}
