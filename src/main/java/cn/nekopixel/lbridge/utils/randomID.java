package cn.nekopixel.lbridge.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class randomID {
    private static final int BLOCK_SIZE = 3;
    private static final int MINIMUM_OUTPUT_SIZE = BLOCK_SIZE * 2;
    private static final long ERROR = -1L;

    private final int shuffleSecret;
    private final String[] shuffle = new String[1000];
    private final Map<String, Integer> unshuffle = new HashMap<>(1000);

    public randomID(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Random ID secret cannot be null.");
        }
        String[] parts = input.split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Random ID secret must contain ':' separating the key and mapping.");
        }
        try {
            this.shuffleSecret = Integer.parseInt(parts[0]);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Random ID secret prefix must be numeric.", ex);
        }
        buildShuffleTables(parts[1]);
    }

    public String convert(long id) {
        try {
            long secretValue = id + shuffleSecret;
            if (secretValue < 0) {
                throw new IllegalStateException("Negative secret value.");
            }
            String padded = leftPadInflate(Long.toString(secretValue));
            StringBuilder output = new StringBuilder(Math.max(padded.length(), MINIMUM_OUTPUT_SIZE));
            boolean firstBlock = true;
            for (int i = 0; i < padded.length(); i += BLOCK_SIZE) {
                String block = padded.substring(i, i + BLOCK_SIZE);
                if (!firstBlock) {
                    if ("000".equals(block)) {
                        output.append('m');
                    } else if (block.startsWith("00")) {
                        output.append('v');
                    } else if (block.charAt(0) == '0') {
                        output.append('z');
                    }
                }
                output.append(shuffleIn(block));
                firstBlock = false;
            }
            return output.toString().toUpperCase(Locale.ROOT);
        } catch (RuntimeException ex) {
            return "error";
        }
    }

    public long reveal(String input) {
        if (input == null) {
            return ERROR;
        }
        try {
            String str = input.toLowerCase(Locale.ROOT);
            StringBuilder output = new StringBuilder(Math.max(str.length(), MINIMUM_OUTPUT_SIZE));
            StringBuilder currentBlock = new StringBuilder(BLOCK_SIZE);
            int pad = 0;

            for (int i = 0; i < str.length(); i++) {
                char ch = str.charAt(i);
                switch (ch) {
                    case 'm':
                        pad = 3;
                        break;
                    case 'v':
                        pad = 2;
                        break;
                    case 'z':
                        pad = 1;
                        break;
                    default:
                        currentBlock.append(ch);
                        if (currentBlock.length() >= BLOCK_SIZE) {
                            output.append(shuffleOut(currentBlock.toString(), pad));
                            currentBlock.setLength(0);
                            pad = 0;
                        }
                        break;
                }
            }

            if (currentBlock.length() > 0) {
                throw new IllegalStateException("Incomplete block in input.");
            }

            long value = Long.parseLong(output.toString());
            long result = value - shuffleSecret;
            if (result < 0) {
                return ERROR;
            }
            return result;
        } catch (RuntimeException ex) {
            return ERROR;
        }
    }

    public String getInfo() {
        return "Secret=" + shuffleSecret + ", mappings=" + unshuffle.size();
    }

    private String leftPadInflate(String id) {
        int pad = id.length() % BLOCK_SIZE;
        switch (pad) {
            case 1:
                return "00" + id;
            case 2:
                return "0" + id;
            default:
                return id;
        }
    }

    private String leftPadDeflate(int pad, int result) {
        switch (pad) {
            case 0:
                return Integer.toString(result);
            case 1:
                return "0" + result;
            case 2:
                return "00" + result;
            case 3:
                return "000";
            default:
                throw new IllegalArgumentException("Invalid pad length: " + pad);
        }
    }

    private void buildShuffleTables(String str) {
        if (str.length() < BLOCK_SIZE * 1000) {
            throw new IllegalArgumentException("Random ID mapping must be at least 3000 characters long.");
        }
        for (int i = 0; i <= 999; i++) {
            int idx = i * BLOCK_SIZE;
            int end = idx + BLOCK_SIZE;
            if (end > str.length()) {
                throw new IllegalArgumentException("Random ID mapping is incomplete.");
            }
            String hex = str.substring(idx, end);
            shuffle[i] = hex;
            unshuffle.put(hex.toLowerCase(Locale.ROOT), i);
        }
    }

    private String shuffleIn(String block) {
        int index = Integer.parseInt(block);
        if (index < 0 || index >= shuffle.length) {
            throw new IllegalArgumentException("Block out of range: " + block);
        }
        String result = shuffle[index];
        if (result != null) {
            return result;
        }
        throw new IllegalArgumentException("No shuffle input for " + block);
    }

    private String shuffleOut(String block, int pad) {
        Integer value = unshuffle.get(block.toLowerCase(Locale.ROOT));
        if (value != null) {
            return leftPadDeflate(pad, value);
        }
        throw new IllegalArgumentException("No shuffle output for " + block);
    }
}
