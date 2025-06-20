// 首先我要声明一下，Litebans 的这个 idRandom 是
// The ID of the punishment in the database, converted to a randomized, unpredictable, but fully reversible ID
// 所以它可以说是完全没有正确答案，无法 100% 还原算法且无法每次计算都相同
// 所以我只保证这个东西它可以还原出数据库里的自增 ID，不要见怪说怎么跟别的子服算的不一样
// 如果你有意见，你来写一个更好的

package cn.nekopixel.lbridge.utils;

public class randomID {
    private final int xorKey;
    private final int offset;

    public randomID(long seed, int offset) {
        this.offset = offset;
        
        if (seed == 0) {
            seed = System.currentTimeMillis();
        }
        this.xorKey = generateXorKey(seed);
    }
    
    public randomID(long seed) {
        this(seed, 12500);
    }

    private int generateXorKey(long seed) {
        long hash = seed;
        hash ^= hash >>> 33;
        hash *= 0x9E3779B97F4A7C15L;
        hash ^= hash >>> 33;
        hash *= 0x9E3779B97F4A7C15L;
        hash ^= hash >>> 33;
        
        int key = (int)(hash & 0xFFFFFFFFL);
        return key != 0 ? key : 0x9A7B3C2D;
    }

    public String convert(long id) {
        try {
            int offsetId = (int)(id + offset);
            int encoded = offsetId ^ xorKey;
            String result = String.format("%08X", encoded);
            
            return result;
        } catch (Exception e) {
            return "INVALID";
        }
    }

    public long reveal(String code) {
        if (code == null || code.length() != 8) {
            return -1;
        }

        try {
            int encoded = (int)Long.parseLong(code.toLowerCase(), 16);
            int offsetId = encoded ^ xorKey;
            return offsetId - offset;
        } catch (Exception e) {
            return -1;
        }
    }
    
    public String getInfo() {
        return String.format("XOR Key: 0x%08X, Offset: %d", xorKey, offset);
    }
}