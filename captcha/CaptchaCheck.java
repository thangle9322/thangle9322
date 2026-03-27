package captcha;

import player.Player;

public class CaptchaCheck {

public static boolean canUseSkill(Player player) {
    if (CaptchaManager.getInstance().containsCaptcha(player.getSession().userId)) {
        return false; // Block skill usage
    }
    return true;
}

public static boolean canUseItem(Player player) {
    if (CaptchaManager.getInstance().containsCaptcha(player.getSession().userId)) {
        return false; // Block item usage
    }
    return true;
}

public static boolean canBuyItem(Player player) {
    if (CaptchaManager.getInstance().containsCaptcha(player.getSession().userId)) {
        return false; // Block purchasing
    }
    return true;
}

}
