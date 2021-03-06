package site.hanschen.bytecode.java.model.constant;

import site.hanschen.bytecode.java.ClassFile;
import sun.misc.FloatConsts;

import java.nio.ByteBuffer;

/**
 * 存储的是源文件中出现的 float 型数据的值
 *
 * @author chenhang
 */
public class FloatInfo extends Constant {

    public float value;

    public FloatInfo(short tag, float value) {
        super(tag);
        this.value = value;
    }

    @Override
    public String getTag() {
        return "Float";
    }

    @Override
    public String getValue() {
        return String.valueOf(value);
    }

    @Override
    public String getComment(Constant[] constantPool) {
        return null;
    }

    public static class Parser implements ConstantParser<FloatInfo> {

        @Override
        public boolean match(short tag) {
            return tag == ClassFile.CONSTANT_Float;
        }

        @Override
        public FloatInfo create(ByteBuffer buffer) {
            short tag = buffer.get();
            int bytes = buffer.getInt();
            int s = ((bytes >> 31) == 0) ? 1 : -1;
            int e = ((bytes >> 23) & 0xff);
            int m = (e == 0) ? (bytes & 0x7fffff) << 1 : (bytes & 0x7fffff) | 0x800000;

            float value = s * m * powerOfTwoF(e - 150);
            return new FloatInfo(tag, value);
        }

        static float powerOfTwoF(int n) {
            assert (n >= FloatConsts.MIN_EXPONENT && n <= FloatConsts.MAX_EXPONENT);
            return Float.intBitsToFloat(((n + FloatConsts.EXP_BIAS) << (FloatConsts.SIGNIFICAND_WIDTH - 1)) & FloatConsts.EXP_BIT_MASK);
        }
    }
}
