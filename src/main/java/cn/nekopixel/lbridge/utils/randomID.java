// 首先我要声明一下，Litebans 的这个 idRandom 是
// The ID of the punishment in the database, converted to a randomized, unpredictable, but fully reversible ID
// 所以它可以说是完全没有正确答案，无法 100% 还原算法且无法每次计算都相同
// 所以我只保证这个东西它可以还原出数据库里的自增 ID，不要见怪说怎么跟别的子服算的不一样
// 如果你有意见，你来写一个更好的

package cn.nekopixel.lbridge.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class randomID {
    private final int offset;
    private final Map<Integer, String> numToCode = new HashMap<>();
    private final Map<String, Integer> codeToNum = new HashMap<>();

    public randomID(long seed, int offset) {
        this.offset = offset;
        initCodes(seed == 0 ? System.currentTimeMillis() : seed);
    }

    public randomID(long seed) {
        this(seed, 12500);
    }

    private void initCodes(long seed) {
        Random r = new Random(seed);
        for (int i = 0; i <= 999; i++) {
            String code;
            do {
                code = Integer.toHexString(r.nextInt(0x1000));
                while (code.length() < 3) code = "0" + code;
            } while (codeToNum.containsKey(code));
            numToCode.put(i, code);
            codeToNum.put(code, i);
        }
    }

    public String convert(long id) {
        long value = id + offset;
        String numStr = String.valueOf(value);
        int padding = (3 - numStr.length() % 3) % 3;
        numStr = "0".repeat(padding) + numStr;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numStr.length(); i += 3) {
            int num = Integer.parseInt(numStr.substring(i, i + 3));
            sb.append(numToCode.get(num));
        }
        return sb.toString().toUpperCase(Locale.ROOT);
    }

    public long reveal(String code) {
        if (code == null) return -1;
        code = code.toLowerCase(Locale.ROOT);
        if (code.length() % 3 != 0) return -1;

        StringBuilder numStr = new StringBuilder();
        for (int i = 0; i < code.length(); i += 3) {
            Integer num = codeToNum.get(code.substring(i, i + 3));
            if (num == null) return -1;
            numStr.append(String.format("%03d", num));
        }

        try {
            long value = Long.parseLong(numStr.toString());
            return value - offset;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public String getInfo() {
        return "Offset=" + offset + ", mappings=" + numToCode.size();
    }
}
