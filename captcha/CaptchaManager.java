package captcha;

import java.util.concurrent.ConcurrentHashMap;
import player.Player;
import services.Service;
import utils.Logger;

public class CaptchaManager {

    private static volatile CaptchaManager instance;
    private static final Object lock = new Object();

    private final ConcurrentHashMap<Integer, CaptchaSession> activeCaptchas;

    private static class CaptchaSession {

        final CaptchaResult captchaResult;

        CaptchaSession(CaptchaResult captchaResult) {
            this.captchaResult = captchaResult;
        }

        void dispose() {
            if (captchaResult != null && !captchaResult.isDisposed()) {
                captchaResult.dispose();
            }
        }
    }

    private CaptchaManager() {
        this.activeCaptchas = new ConcurrentHashMap<>();
    }

    public static CaptchaManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new CaptchaManager();
                }
            }
        }
        return instance;
    }

    public void generateCaptchaForPlayer(Player player) {
    try {
        if (player == null || player.getSession() == null) {
            System.out.println("[CAPTCHA ERROR] Player is null in generateCaptchaForPlayer");
            return;
        }
        
        int sessionId = player.getSession().userId;
        
        // ← THÊM: Check xem đã có captcha cũ không, xóa nó trước
        if (containsCaptcha(sessionId)) {
            System.out.println("[CAPTCHA WARNING] Duplicate captcha found for " + player.name + ", removing old one");
            removeCaptcha(sessionId);  // ← Clear cái cũ
        }
        
        int newSessionId = generateCaptcha(player, player.getSession().zoomLevel);
        CaptchaResult captcha = getCaptcha(newSessionId);
        if (captcha != null) {
            try {
                Service.gI().sendCaptcha(player);
                System.out.println("[CAPTCHA] Captcha generated successfully for: " + player.name);
            } catch (Exception sendEx) {
                System.out.println("[CAPTCHA ERROR] Failed to send captcha: " + sendEx.getMessage());
                sendEx.printStackTrace();
            }
        } else {
            System.out.println("[CAPTCHA ERROR] getCaptcha returned null for player: " + player.name);
        }

    } catch (Exception e) {
        System.out.println("[CAPTCHA ERROR] generateCaptchaForPlayer failed: " + e.getMessage());
        e.printStackTrace();
    }
}

    public int generateCaptcha(Player player, int zoomLevel) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        try {
            int sessionId = player.getSession().userId;
            CaptchaResult captchaResult = CaptchaGenerator.createCaptchaImage(zoomLevel);
            CaptchaSession session = new CaptchaSession(captchaResult);
            activeCaptchas.put(sessionId, session);
            return sessionId;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CAPTCHA", e);
        }
    }

    public boolean containsCaptcha(int sessionId) {
        return activeCaptchas.containsKey(sessionId);
    }

    public CaptchaResult getCaptcha(int sessionId) {
        CaptchaSession session = activeCaptchas.get(sessionId);
        if (session == null) {
            return null;
        }
        return session.captchaResult;
    }

    public void handlePlayerCaptchaInput(Player player, char input) {
        int sessionId = player.getSession().userId;
        CaptchaSession session = activeCaptchas.get(sessionId);
        if (session == null) {
            return;
        }
        CaptchaResult captchaResult = session.captchaResult;
        if (captchaResult.isDisposed()) {
            return;
        }
        boolean completed = captchaResult.addInput(input);
        if (completed) {
            removeCaptcha(sessionId);
            Service.gI().sendFinishCaptcha(player);
        } else if (captchaResult.captchaEnterd.length() == 6) {
            if (captchaResult.captchaFailCount >= 10) {
                captchaResult.captchaFailCount = 0;
                generateCaptchaForPlayer(player);
            } else {
                captchaResult.captchaFailCount++;
            }
        }
    }

    public void removeCaptcha(int sessionId) {
    try {
        CaptchaSession session = activeCaptchas.get(sessionId);
        if (session != null) {
            removeSessionAndCleanup(sessionId, session);
            System.out.println("[CaptchaManager] Removed captcha for sessionId: " + sessionId);
        }
    } catch (Exception e) {
        System.out.println("[CaptchaManager] Error removing captcha: " + e.getMessage());
        e.printStackTrace();
    }
}

    private void removeSessionAndCleanup(int sessionId, CaptchaSession session) {
    try {
        activeCaptchas.remove(sessionId);
        session.dispose();
        System.out.println("[CaptchaManager] Cleanup completed for sessionId: " + sessionId);
    } catch (Exception e) {
        System.out.println("[CaptchaManager] Cleanup error: " + e.getMessage());
    }
}
}
