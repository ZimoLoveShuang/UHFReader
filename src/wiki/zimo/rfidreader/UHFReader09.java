package wiki.zimo.rfidreader;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;

public interface UHFReader09 extends Library{
	UHFReader09 INSTANCE = Native.loadLibrary("UHFReader09", UHFReader09.class);
	
	public int AutoOpenComPort(IntByReference port,ByteByReference ComAddr,byte baud,IntByReference handle);
	public int GetReaderInformation(ByteByReference ComAddr, byte[] VersionInfo, ByteByReference ReaderType, byte[] TrType, ByteByReference dmaxfre, ByteByReference dminfre, ByteByReference powerdBm, ByteByReference ScanTime, int PortHandle);
	public int CloseSpecComPort(int port);
	public int OpenComPort(int port,ByteByReference ComAddr,byte baud,IntByReference handle);
}
