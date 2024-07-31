package xin.vanilla.util.mcstatus;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import net.mamoe.mirai.utils.MiraiLogger;
import xin.vanilla.VanillaKanri;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class McQuery {
    private static final MiraiLogger logger = VanillaKanri.INSTANCE.getLogger();

    private final String serverName;
    private final String serverAddress;
    private int queryPort = 25565; // 默认查询接口
    private JSONObject serverJSON = new JSONObject();
    public static final String ERROR_MSG_LOADING = "LOADING";
    public static final String ERROR_MSG_UNKNOWN_HOST = "UNKNOWN_HOST";
    public static final String ERROR_MSG_CONNECT_FAILED = "FAILED";
    public static final String ERROR_MSG_UNKNOWN_RESPONSE = "UNKNOWN_RESPONSE";

    public McQuery(String name, String address) throws URISyntaxException {
        serverName = name;
        URI uri = new URI("VanillaMCQuery://" + address);
        serverAddress = uri.getHost();
        if (uri.getPort() > 0) {
            queryPort = uri.getPort();
        }
        setError(ERROR_MSG_LOADING);
    }

    public void setDescription(String msg) {
        try {
            serverJSON.put("description", msg);
        } catch (JSONException e) {
            logger.error(e);
        }
    }

    public void setError(String msg) {
        try {
            serverJSON.put("error", msg);
        } catch (JSONException e) {
            logger.error(e);
        }
    }

    public String error() {
        return serverJSON.getString("error");
    }

    public String serverIp() {
        return serverAddress;
    }

    public int serverPort() {
        return queryPort;
    }

    public String serverAddress() {
        StringBuilder result = new StringBuilder();
        result.append(serverAddress);
        // 若不是默认端口
        if (queryPort != 25565) {
            result.append(":");
            result.append(queryPort);
        }
        return result.toString();
    }

    public String serverName() {
        return serverName;
    }

    public int maxPlayers() {
        JSONObject players = serverJSON.getJSONObject("players");
        if (players == null) {
            return 0;
        }
        return players.getIntValue("max");
    }

    public int onlinePlayers() {
        JSONObject players = serverJSON.getJSONObject("players");
        if (players == null) {
            return 0;
        }
        return players.getIntValue("online");
    }

    public List<String> playerList() {
        List<String> result = new ArrayList<>();
        JSONObject players = serverJSON.getJSONObject("players");
        if (players == null) {
            return result;
        }
        JSONArray sample = players.getJSONArray("sample");
        if (sample == null) {
            return result;
        }
        int pos = 0;
        while (pos < sample.size()) {
            JSONObject entry = sample.getJSONObject(pos++);
            String username = entry.getString("name");
            result.add(username);
        }
        Collections.sort(result);
        // logger.debug("playerList() returning ".concat(result.toString()));
        return result;
    }

    public String playerListString(String separator) {
        List<String> playerList = this.playerList();
        return String.join(separator, playerList);
    }

    public String playerListString() {
        return this.playerListString(", ");
    }

    public String serverVersion() {
        JSONObject version = serverJSON.getJSONObject("version");
        if (version == null) {
            return "";
        }
        return version.getString("name");
    }

    public String description() {
        StringBuilder result = new StringBuilder();
        JSONArray descExtra = null;
        String desc = serverJSON.getString("description");
        // logger.info("desc: " + desc);
        JSONObject descriptionObj = serverJSON.getJSONObject("description");
        if (descriptionObj != null) {
            desc = descriptionObj.getString("text");
            descExtra = descriptionObj.getJSONArray("extra");
        }
        // logger.info("desc: " + desc);
        int curChar = 0;
        if (descExtra != null) {
            while (curChar < descExtra.size()) {
                JSONObject chunk = null;
                try {
                    chunk = descExtra.getJSONObject(curChar++);
                } catch (JSONException ignored) {
                }
                if (chunk != null) {
                    try {
                        String text = chunk.getString("text");
                        result.append(text);
                    } catch (JSONException e) {
                        logger.error(e);
                    }
                }
            }
        } else {
            while (curChar < desc.length()) {
                char theChar = desc.charAt(curChar++);
                if (theChar == '§') {
                    curChar++;
                } else {
                    result.append(theChar);
                }
            }
        }
        return result.toString();
    }

    /**
     * See <a href="http://wiki.vg/Protocol">Status Ping</a>
     */
    public void query() {
        Socket socket;
        try {
            socket = new Socket(serverAddress, queryPort);
            socket.setSoTimeout(10000);
        } catch (UnknownHostException e) {
            // 服务器地址有误
            setError(ERROR_MSG_UNKNOWN_HOST);
            return;
        } catch (IllegalArgumentException | IOException e) {
            // 已离线或未启用查询
            setError(ERROR_MSG_CONNECT_FAILED);
            // setDescription("Error: " + e.getLocalizedMessage());
            return;
        }
        OutputStream out;
        InputStream in;
        try {
            out = socket.getOutputStream();
            in = socket.getInputStream();
            // 数据包的总长度
            out.write(6 + serverAddress.length());
            // 数据包ID
            out.write(0);
            // 协议版本
            out.write(4);
            // 服务器地址长度
            out.write(serverAddress.length());
            // 服务器UTF-8地址
            out.write(serverAddress.getBytes());
            // 端口高位字节
            out.write((queryPort & 0xFF00) >> 8);
            // 端口低位字节
            out.write(queryPort & 0x00FF);
            // 下一个状态标志，1: Status, 2: Login, 3: Transfer
            out.write(1);
            // 状态ping的第一个字节
            out.write(0x01);
            // 状态ping的第一个字节
            out.write(0x00);

            // 整个数据包的大小
            int packetLength = readVarInt(in);
            String serverData;
            if (packetLength < 11) {
                logger.info(String.format("%s, %s: 数据包长度过短: %d%n", serverName, serverAddress, packetLength));
                // 来自服务器的响应无效(服务器可能正在重新启动)
                setError(ERROR_MSG_UNKNOWN_RESPONSE);
                in.close();
                out.close();
                socket.close();
                return;
            }
            // 忽略数据包类型, 只用取一个类型
            final int packetType = in.read();
            // logger.info(String.format("%s, %s: 数据包类型: %d%n", serverName, serverAddress, packetType));
            int jsonLength = readVarInt(in);
            if (jsonLength < 0) {
                in.close();
                out.close();
                socket.close();
                return;
            }
            // 安全起见, 多取10字节
            byte[] buffer = new byte[jsonLength + 10];
            int bytesRead = 0;
            do {
                bytesRead += in.read(buffer, bytesRead, jsonLength - bytesRead);
            } while (bytesRead < jsonLength);
            serverData = new String(buffer, 0, bytesRead);
            serverJSON = JSON.parseObject(serverData);
            in.close();
            out.close();
            socket.close();
            setError(null);
        } catch (JSONException | IOException e) {
            logger.error(e);
        }
    }

    private int readVarInt(InputStream in) {
        int theInt = 0;
        for (int i = 0; i < 6; i++) {
            int theByte;
            try {
                theByte = in.read();
            } catch (IOException e) {
                logger.error(e);
                return 0;
            }

            theInt |= (theByte & 0x7F) << (7 * i);
            if (theByte == 0xffffffff) {
                logger.warning(String.format("readVarInt: 收到意外的字节值: %#x%n", theByte));
                return -1;
            }
            if ((theByte & 0x80) != 128) {
                break;
            }
        }
        return theInt;
    }
}
