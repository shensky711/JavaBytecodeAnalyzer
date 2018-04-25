package site.hanschen.bytecode.java;

import org.apache.commons.io.IOUtils;
import site.hanschen.bytecode.java.model.*;
import site.hanschen.bytecode.java.utils.Logger;
import site.hanschen.bytecode.java.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author chenhang
 */
public class ClassFileReader {

    private static List<ConstantElementParser> PARSERS = new ArrayList<>();

    static {
        PARSERS.add(new ClassInfo.Parser());
        PARSERS.add(new FieldRefInfo.Parser());
        PARSERS.add(new MethodRefInfo.Parser());
        PARSERS.add(new InterfaceMethodRef.Parser());
        PARSERS.add(new StringInfo.Parser());
        PARSERS.add(new IntegerInfo.Parser());
        PARSERS.add(new FloatInfo.Parser());
        PARSERS.add(new LongInfo.Parser());
        PARSERS.add(new DoubleInfo.Parser());
        PARSERS.add(new NameAndTypeInfo.Parser());
        PARSERS.add(new Utf8Info.Parser());
        PARSERS.add(new MethodHandleInfo.Parser());
        PARSERS.add(new MethodTypeInfo.Parser());
        PARSERS.add(new InvokeDynamicInfo.Parser());
    }

    public ClassFileReader(File file) throws IOException {
        this(new FileInputStream(file));
    }

    public ClassFileReader(InputStream input) throws IOException {
        this(IOUtils.toByteArray(input));
    }

    public ClassFileReader(byte[] data) {
        this(ByteBuffer.wrap(data));
    }

    public ClassFileReader(ByteBuffer buffer) {
        Logger.d(Utils.dumpHex(buffer.array()));
        buffer.position(0);
        buffer = buffer.asReadOnlyBuffer().order(ByteOrder.BIG_ENDIAN);

        // check magic number.
        int magic = buffer.getInt();
        if (ClassFile.MAGIC_CLASS_FILE != magic) {
            throw new IllegalArgumentException(String.format("invalid java class file, magic number: 0x%X", magic));
        }

        // show version.
        int minor = buffer.getShort();
        int major = buffer.getShort();
        Logger.d("bytecode version: %d.%d", major, minor);

        // constant_pool_count
        int constantPoolCount = buffer.getShort();
        Logger.d("constantPoolCount: %d", constantPoolCount);

        // attention, The constant_pool table is indexed from 1 to constant_pool_count-1
        ConstantElement[] constantPool = new ConstantElement[constantPoolCount];
        for (int i = 1; i < constantPoolCount; i++) {
            short tag = buffer.get();
            // reset position at tag data
            buffer.position(buffer.position() - 1);
            ConstantElementParser parser = null;
            for (ConstantElementParser p : PARSERS) {
                if (p.match(tag)) {
                    parser = p;
                    break;
                }
            }
            if (parser == null) {
                throw new IllegalStateException("unsupported tag: " + tag);
            }

            ConstantElement element = parser.create(buffer);
            constantPool[i] = element;
            Logger.d("index: %d, tag:%d, element:%s", i, element.tag, element.toString());
        }

        // parse access flags
        short accessFlags = buffer.getShort();
        Logger.d("accessFlags: %d", accessFlags);

        // get class name
        int thisClassIndex = buffer.getShort();
        ClassInfo classInfo = (ClassInfo) constantPool[thisClassIndex];
        Utf8Info classname = (Utf8Info) constantPool[classInfo.nameIndex];
        Logger.d("this class name: %s", classname.value);

        // parse super class name
        int superClass = buffer.getShort();
        if (superClass == 0) {
            // this class file must represent the class Object, the only class or interface without a direct superclass.
        } else {
            classInfo = (ClassInfo) constantPool[superClass];
            classname = (Utf8Info) constantPool[classInfo.nameIndex];
            Logger.d("super class name: %s", classname.value);
        }

        // parse interfaces
        int interfaceCount = buffer.getShort();
        Logger.d("interface count: %d", interfaceCount);
        if (interfaceCount > 0) {
            int interfaceIndex;
            for (int i = 0; i < interfaceCount; i++) {
                interfaceIndex = buffer.getShort();
                classInfo = (ClassInfo) constantPool[interfaceIndex];
                classname = (Utf8Info) constantPool[classInfo.nameIndex];
                Logger.d("interface name: %s", classname.value);
            }
        }

        // parse fields count
        int fieldsCount = buffer.getShort();
        Logger.d("fields count: %d", fieldsCount);
        if (fieldsCount > 0) {
            FieldInfo[] fieldInfo = new FieldInfo[fieldsCount];
            for (int i = 0; i < fieldsCount; i++) {
                fieldInfo[i] = FieldInfo.readFrom(buffer);
                Logger.d("field: %s", ((Utf8Info) constantPool[fieldInfo[i].nameIndex]).value);
            }
        }
    }
}