package com.swust.utils.common;

import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

public class AppJwtUtil {
    public static final String CURRENT_USER = "current_user";

    // TOKEN的有效期一天（S）
    private static final int TOKEN_TIME_OUT = 3_600;
    // 加密KEY
    private static final String TOKEN_ENCRY_KEY = "MDk4ZjZiY2Q0NjIxZDM3M2NhZGU0ZTgzMjYyN2I0ZjY";
    // 最小刷新间隔(S)
    private static final int REFRESH_TIME = 300;

    // 生产ID
    public static String getToken(Object obj) {
        Map<String, Object> claimMaps = new HashMap<>();
        claimMaps.put(CURRENT_USER, JSON.toJSONString(obj));
        long currentTime = System.currentTimeMillis();
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date(currentTime))  //签发时间
                .setSubject("system")  //说明
                .setIssuer("heima") //签发者信息
                .setAudience("app")  //接收用户
                .compressWith(CompressionCodecs.GZIP)  //数据压缩方式
                .signWith(SignatureAlgorithm.HS512, generalKey()) //加密方式
                .setExpiration(new Date(currentTime + TOKEN_TIME_OUT * 1000))  //过期时间戳
                .addClaims(claimMaps) //claim信息
                .compact();
    }

    /**
     * 获取token中的claims信息
     *
     * @param token
     * @return
     */
    private static Jws<Claims> getJws(String token) {
        return Jwts.parser()
                .setSigningKey(generalKey())
                .parseClaimsJws(token);
    }

    /**
     * 获取payload body信息
     *
     * @param token
     * @return
     */
    public static Claims getClaimsBody(String token) throws Exception {
        try {
            Jws<Claims> jws = getJws(token);
            return jws.getBody();
        } catch (ExpiredJwtException e) {
            // 处理JWT过期的异常
            throw new Exception("Token已过期");
        } catch (Exception e) {
            // 处理其他可能的异常，如签名验证失败等
            throw new Exception("无法解析Token: " + e.getMessage());
        }
    }

    /**
     * 获取hearder body信息
     *
     * @param token
     * @return
     */
    public static JwsHeader getHeaderBody(String token) {
        return getJws(token).getHeader();
    }

    /**
     * 是否过期
     *
     * @param claims
     * @return -1：有效，0：有效，1：过期，2：过期
     */
    public static int verifyToken(Claims claims) {
        if (claims == null) {
            return 1;
        }
        try {
            claims.getExpiration()
                    .before(new Date());
            // 需要自动刷新TOKEN
            if ((claims.getExpiration().getTime() - System.currentTimeMillis()) > REFRESH_TIME * 1000) {
                return -1;
            } else {
                return 0;
            }
        } catch (ExpiredJwtException ex) {
            return 1;
        } catch (Exception e) {
            return 2;
        }
    }

    /**
     * 由字符串生成加密key
     *
     * @return
     */
    public static SecretKey generalKey() {
        byte[] encodedKey = Base64.getEncoder().encode(TOKEN_ENCRY_KEY.getBytes());
        SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        return key;
    }

    public static void main(String[] args) {
       /* Map map = new HashMap();
        map.put("id","11");*/
        System.out.println(AppJwtUtil.getToken(1102L));
        Jws<Claims> jws = AppJwtUtil.getJws("eyJhbGciOiJIUzUxMiIsInppcCI6IkdaSVAifQ.H4sIAAAAAAAA_zXLSwoDIRBF0b3UWMFSY382EzRWEwOK-IEOTe89OsisLvXOBZ8WYAd0xmmFD05-Ia43gdxprzgeq1ztQehJAoNgG-y4KL0ZJaRgULsbun5rozj_tY58U4h2lO1-lM153HTmvzRmylcvhVJ79koF9gvC2CIKySDZSNP5GBLc9w-CllDxowAAAA.EsNLm8zYT6syqtMoxcxy20SFp7wzH88JfuGl8Sa_wOdbw7CqTf1P6XN02UyfBfel_jdBA-8bScQRfjRc5Uw2qg");
        Claims claims = jws.getBody();
        System.out.println(claims.get(CURRENT_USER));

    }

}
