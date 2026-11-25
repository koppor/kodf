package io.github.koppor.kodf;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

// [impl->dsn~extent-fingerprint~1]
/**
 * Fingerprints where a file's data lies on disk, read with the FIEMAP ioctl of Linux. Two files with the
 * same fingerprint share their data: on btrfs after a reflink copy, a dedupe, or in a snapshot.
 */
final class Extents {

    private static final boolean LINUX = "Linux".equals(System.getProperty("os.name"));

    private static final long FS_IOC_FIEMAP = 0xC020660BL;
    private static final int FIEMAP_FLAG_SYNC = 0x1;
    private static final int FIEMAP_EXTENT_LAST = 0x1;

    /**
     * Unknown, delayed, compressed, encrypted, unaligned and inline extents: their physical address does
     * not tell which bytes they hold.
     */
    private static final int UNCLEAR = 0x2 | 0x4 | 0x8 | 0x80 | 0x100 | 0x200 | 0x400;

    private static final int HEADER = 32;
    private static final int EXTENT = 56;
    private static final int PER_CALL = 64;

    private Extents() {}

    /** Returns {@code null} when the location is unknown, for example on a filesystem without FIEMAP. */
    static String fingerprint(Path file) {
        if (!LINUX) {
            return null;
        }
        try (Arena arena = Arena.ofConfined()) {
            int fd = (int) Libc.OPEN.invokeExact(arena.allocateFrom(file.toString()), 0);
            if (fd < 0) {
                return null;
            }
            try {
                return fingerprint(fd, arena.allocate(HEADER + EXTENT * PER_CALL, 8));
            } finally {
                int _ = (int) Libc.CLOSE.invokeExact(fd);
            }
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    private static String fingerprint(int fd, MemorySegment map) throws Throwable {
        MessageDigest digest = sha256();
        ByteBuffer extent = ByteBuffer.allocate(3 * Long.BYTES);
        long start = 0;
        boolean last = false;
        while (!last) {
            map.fill((byte) 0);
            map.set(JAVA_LONG, 0, start);
            map.set(JAVA_LONG, 8, -1L);
            map.set(JAVA_INT, 16, FIEMAP_FLAG_SYNC);
            map.set(JAVA_INT, 24, PER_CALL);
            if ((int) Libc.IOCTL.invokeExact(fd, FS_IOC_FIEMAP, map) != 0) {
                return null;
            }
            int mapped = map.get(JAVA_INT, 20);
            if (mapped == 0) {
                break;
            }
            for (int i = 0; i < mapped; i++) {
                long at = HEADER + (long) EXTENT * i;
                long logical = map.get(JAVA_LONG, at);
                long length = map.get(JAVA_LONG, at + 16);
                int flags = map.get(JAVA_INT, at + 40);
                if ((flags & UNCLEAR) != 0) {
                    return null;
                }
                digest.update(extent.clear()
                        .putLong(logical)
                        .putLong(map.get(JAVA_LONG, at + 8))
                        .putLong(length)
                        .flip());
                start = logical + length;
                last = (flags & FIEMAP_EXTENT_LAST) != 0;
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Linked only on Linux. */
    private static final class Libc {
        private static final Linker LINKER = Linker.nativeLinker();
        static final MethodHandle OPEN = handle("open", FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT));
        static final MethodHandle CLOSE = handle("close", FunctionDescriptor.of(JAVA_INT, JAVA_INT));
        static final MethodHandle IOCTL = handle(
                "ioctl",
                FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_LONG, ADDRESS),
                Linker.Option.firstVariadicArg(2));

        private static MethodHandle handle(String name, FunctionDescriptor descriptor, Linker.Option... options) {
            return LINKER.downcallHandle(LINKER.defaultLookup().findOrThrow(name), descriptor, options);
        }
    }
}
