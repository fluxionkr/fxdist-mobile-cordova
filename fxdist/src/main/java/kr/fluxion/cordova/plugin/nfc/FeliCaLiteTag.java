package kr.fluxion.cordova.plugin.nfc;

import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.NfcF;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author tomorrowkey@gmail.com
 */
public class FeliCaLiteTag {

    protected static final String TAG = FeliCaLiteTag.class.getSimpleName();
    private static final boolean D = true;
    //private static final boolean D = false;

    /**
     * 書き込みコマンド
     */
    private static final byte WRITE_WITHOUT_ENCRYPTION = (byte) 0x08;
    private static final byte READ_WITHOUT_ENCRYPTION = (byte) 0x06;
    private static final byte POLLING_WITHOUT_ENCRYPTION = (byte) 0x00;
    private static final byte ATR_WITHOUT_ENCRYPTION = (byte) 0x08;

    /**
     * タグ
     */
    private NfcF mNfcF;


    /**
     * TODO UnsupportTagException が発生するパターンに、FeliCa Lite判定を追加する
     *
     * @param tag
     * @throws FeliCaLiteTag.UnsupportTagException FeliCa系のタグでない場合に発生します
     */
    public FeliCaLiteTag(Tag tag) throws UnsupportTagException {
        if (tag == null) throw new IllegalArgumentException();

        mNfcF = NfcF.get(tag);

        if (mNfcF == null) throw new UnsupportTagException();
    }

    /**
     * SYS_OPのNDEFフラグを変更します。<br>
     * 大部分にマジックナンバーを使っているため0次発行の場合のみで動くという制限があります <br>
     * FIXME SYS_OP 以外はタグから現在の値を取得してその値を使うようにしないと書き込みエラーが発生する場合がある
     *
     * @param isNdef true にした場合NDEF化される。false にした場合NDEFではなくなる。
     * @throws IOException
     * @throws TagLostException
     */
    public void applyNdefFlag(byte[] idm, boolean isNdef) throws TagLostException, IOException {
        if (D) Log.d(TAG, "applyNdefFlag");
        // @formatter:off
        byte[] data = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x01, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,};
        // @formatter:on
        data[3] = isNdef ? (byte) 0x01 : (byte) 0;

        // FIXME レスポンスを握りつぶしているので、どうにかする
        writeWithoutEncryption(idm, new int[]{0x88}, new byte[][]{data});
    }

    /**
     * NdefMessageを書き込みます
     *
     * @param idm         IDm
     * @param ndefMessage NDEF
     * @throws SizeOverflowException NdefMessageのサイズが大きすぎる場合に発生します
     * @throws TagLostException
     * @throws IOException
     */
    public void writeNdefMessage(byte[] idm, NdefMessage ndefMessage) throws SizeOverflowException, TagLostException, IOException {
        if (D) Log.d(TAG, "writeNdefMessage");
        if (idm == null || idm.length == 0) throw new IllegalArgumentException();
        if (ndefMessage == null) throw new IllegalArgumentException();

        byte[][] datas = mappingBlock(ndefMessage);

//		for (int blockNumber = 0; blockNumber <= 13; blockNumber++) {
//			// FIXME レスポンスを握りつぶしているので、どうにかする
//			writeWithoutEncryption(idm, blockNumber, datas[blockNumber]);
//		}
        for (int blockNumber = 1; blockNumber <= 13; blockNumber++) {
            // FIXME レスポンスを握りつぶしているので、どうにかする
            writeWithoutEncryption(idm, blockNumber, datas[blockNumber]);
        }
        writeWithoutEncryption(idm, 0, datas[0]);
    }

    public boolean writeBytes(byte[] idm, int blockPos, byte[] bytes) throws SizeOverflowException, TagLostException, IOException {
        if (D) Log.d(TAG, "writeBytes");
        if (idm == null || idm.length == 0) throw new IllegalArgumentException();
        if (bytes == null) throw new IllegalArgumentException();

        byte[][] datas = mappingBlock(bytes);
////		int[] blockNumbers = new int[datas.length];
////		for (int i = 0; i < datas.length; i++) {
////			blockNumbers[i] = i;
////		}
//		
//		int blockCnt=datas.length;
//		int[] blockNumbers = new int[datas.length];
//		for (int i = blockPos; i < (blockPos + datas.length); i++) {
//			blockNumbers[i - blockPos] = i;
//		}
//		
//		byte[] response = writeWithoutEncryption(idm, blockNumbers, datas);
//		
//		if(D) Log.d(TAG, "response = " + ByteUtil.toHexString(response) + ", result = " + (response != null && response.length > 12 && response[10] == 0 && response[11] == 0));
//		return (response != null && response.length > 12 && response[10] == 0 && response[11] == 0);

        //for (int i = 0; i < datas.length; i++) {
        for (int i = 1; i < datas.length; i++) {
            byte[] response = writeWithoutEncryption(idm, i, datas[i]);
        }
        byte[] response = writeWithoutEncryption(idm, 0, datas[0]);
        return true;
    }

    public byte[] writeBlockBytes(byte[] idm, byte bn_h, byte bn_l, byte nBlock, byte[] WrData) throws SizeOverflowException, TagLostException, IOException {
        if (D) Log.d(TAG, "writeBlockBytes");
        if (idm == null || idm.length == 0) throw new IllegalArgumentException();
        int BuffSize = 31;

        if (nBlock == 1) {
            BuffSize = 31;
        } else if (nBlock == 2) {
            BuffSize = 31 + 16 + 2;
        } else if (nBlock == 3) {
            BuffSize = 31 + 16 + 2 + 16 + 2;
        } else if (nBlock == 4) {
            BuffSize = 31 + 16 + 2 + 16 + 2 + 16 + 2;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(BuffSize);

        byteBuffer.put(WRITE_WITHOUT_ENCRYPTION);    // Write Without Encryption
        byteBuffer.put(idm);                        // IDm
        byteBuffer.put((byte) 0x01);                    //Number of Services
        byteBuffer.put((byte) 0x09);                    //Service Code List0(Write)
        byteBuffer.put((byte) 0x00);                    //Service Code List1
        byteBuffer.put((byte) nBlock);                //Number of Blocks
        if (nBlock == 1) {
            byteBuffer.put((byte) bn_h);                //Block List0(Fixed:0x80)
            byteBuffer.put((byte) bn_l);                //Block List1(Block1)
        } else if (nBlock == 2) {
            byteBuffer.put((byte) bn_h);                //Block List0(Fixed)
            byteBuffer.put((byte) bn_l);                //Block List1(Block1)
            byteBuffer.put((byte) bn_h);                //Block List0(Fixed)
            byteBuffer.put((byte) (bn_l + 1));        //Block List1(Block2)
        } else if (nBlock == 3) {
            byteBuffer.put((byte) bn_h);                //Block List0(Fixed)
            byteBuffer.put((byte) bn_l);                //Block List1(Block1)
            byteBuffer.put((byte) bn_h);                //Block List0(Fixed)
            byteBuffer.put((byte) (bn_l + 1));        //Block List1(Block2)
            byteBuffer.put((byte) bn_h);                //Block List0(Fixed)
            byteBuffer.put((byte) (bn_l + 2));        //Block List1(Block3)
        } else if (nBlock == 4) {
            byteBuffer.put((byte) bn_h);                //Block List0(Fixed)
            byteBuffer.put((byte) bn_l);                //Block List1(Block1)
            byteBuffer.put((byte) bn_h);                //Block List0(Fixed)
            byteBuffer.put((byte) (bn_l + 1));        //Block List1(Block2)
            byteBuffer.put((byte) bn_h);                //Block List0(Fixed)
            byteBuffer.put((byte) (bn_l + 2));        //Block List1(Block3)
            byteBuffer.put((byte) bn_h);                //Block List0(Fixed)
            byteBuffer.put((byte) (bn_l + 3));        //Block List1(Block4)
        }

        //Write Data
        byteBuffer.put(WrData);

        byte[] command = byteBuffer.array();
        byte[] response = executeCommand(command);

        return response;
    }

    /**
     * FeliCa Liteの各ブロックにマッピングします
     *
     * @param bytes
     * @return
     * @throws FeliCaLiteTag.SizeOverflowException
     * @throws IOException
     */
    private byte[][] mappingBlock(byte[] bytes) throws SizeOverflowException, IOException {
        int maxTransceiveLen = mNfcF.getMaxTransceiveLength();
        int maxBlockCount = (maxTransceiveLen - 13) / 18;        // (13 = command header, 18 = block List(1) + block data)
//		byte[] ndefMessageBytes = ndefMessage.toByteArray();
        int ndefMessageBytesLength = bytes.length;
        int blockCount = (int) Math.ceil(ndefMessageBytesLength / 16.0);
        if (blockCount > maxBlockCount)
            throw new SizeOverflowException(ndefMessageBytesLength, 16 * maxBlockCount);

        //byte[][] datas = new byte[maxBlockCount][16];
        byte[][] datas = new byte[blockCount][16];
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        try {
            inputStream = new ByteArrayInputStream(bytes);
            int readLength;
            for (int i = 0; i < datas.length; i++) {
                readLength = inputStream.read(datas[i]);
                if (readLength == -1) break;
            }
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                // ignore
                if (D) Log.d(TAG, "mappingBlock(byte) fail!", e);
            }
        }

        return datas;
    }

    /**
     * NdefMessageからFeliCa Liteの各ブロックにマッピングします
     *
     * @param ndefMessage
     * @return
     * @throws SizeOverflowException
     * @throws IOException
     */
    private byte[][] mappingBlock(NdefMessage ndefMessage) throws SizeOverflowException, IOException {
        byte[] ndefMessageBytes = ndefMessage.toByteArray();
        int ndefMessageBytesLength = ndefMessageBytes.length;
        int blockCount = (int) Math.ceil(ndefMessageBytesLength / 16.0);
        if (blockCount > 13) throw new SizeOverflowException(ndefMessageBytesLength, 16 * 13);

        byte[][] datas = new byte[14][16];
        datas[0] = createNdefHeader(ndefMessageBytesLength);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(ndefMessageBytes);
        try {
            inputStream = new ByteArrayInputStream(ndefMessageBytes);
            int readLength;
            for (int i = 1; i < datas.length; i++) {
                readLength = inputStream.read(datas[i]);
                if (readLength == -1) break;
            }
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                // ignore
                if (D) Log.d(TAG, "mappingBlock(Msg) fail!", e);
            }
        }

        return datas;
    }

    private byte[] createNdefHeader(int ndefLength) {
        ByteBuffer buffer = ByteBuffer.allocate(16);

        // Ver
        buffer.put((byte) 0x10);

        // Nbr
        // Read Without Encrypitonで一度に読めるブロック数を指定します
        // FeliCa Liteなので、一度に4ブロック読み込める
        buffer.put((byte) 0x04);

        // Nbw
        // Write Without Encryptionで一度に書き込めるブロック数を指定します
        // FeliCa Liteなので、一度に1ブロック書き込める
        buffer.put((byte) 0x04);

        // Nmaxb
        // NDEFとして使用できるブロック数
        // FeliCa Liteなので、データ領域は13ブロックまで
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x0d);

        // unused
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);

        // WriteF
        // 一枚で完結しているので、0x00
        buffer.put((byte) 0x00);

        // RW Flag
        // Read Writeなので0x01
        buffer.put((byte) 0x01);

        // Ln
        // NDEFデータの長さを指定します
        buffer.put((byte) ((ndefLength >>> 16) & 0xff));
        buffer.put((byte) ((ndefLength >>> 8) & 0xff));
        buffer.put((byte) (ndefLength & 0xff));

        // Checksum
        // チェックサムを指定します
        buffer.put(checksum(buffer.array()));

        return buffer.array();
    }

    /**
     * チェックサムを作成します<br>
     * すべてのバイト配列の合計を計算します
     *
     * @param byteArray
     * @return
     */
    private byte[] checksum(byte[] byteArray) {
        int sum = 0;
        for (byte b : byteArray) {
            sum += b & 0xff;
        }
        return new byte[]{(byte) ((sum >>> 8) & 0xff), (byte) (sum & 0xff)};
    }

    /**
     * Write Without Encryptionコマンドを発行します<br>
     * FeliCa Liteなので、1度のコマンド発行で1ブロックだけ書き込めます
     *
     * @param idm          IDm
     * @param blockNumbers ブロック番号
     * @param datas        書き込みデータ
     * @return レスポンス
     * @throws TagLostException
     * @throws IOException
     */
    public byte[] writeWithoutEncryption(byte[] idm, int[] blockNumbers, byte[][] datas) throws TagLostException, IOException {
        if (idm == null || idm.length == 0) throw new IllegalArgumentException();

        ByteBuffer byteBuffer = ByteBuffer.allocate(252); // transceive length (N-bridge)

        // Write Without Encryption
        byteBuffer.put(WRITE_WITHOUT_ENCRYPTION);

        // IDm
        byteBuffer.put(idm);

        // サービス数
        // FeliCa Liteなので1に固定
        byteBuffer.put((byte) 0x01);

        // サービスコード（リトルエンディアン）
        // 0x00 0x09
        byteBuffer.put((byte) 0x09);
        byteBuffer.put((byte) 0x00);

        // ブロック数
        // FeliCa Liteなので1に固定
//		byteBuffer.put((byte) 0x01);
        byteBuffer.put((byte) blockNumbers.length);

        // ブロックリスト
        // 長さ 2Byteなので1b
        // アクセスモード FeliCa Liteなので000bに固定
        // サービスコード順番 FeliCa Liteなので0000bに固定
        // ブロック番号 引数から指定
        for (int i = 0; i < datas.length; i++) {
            byteBuffer.put((byte) 0x80);
            byteBuffer.put((byte) blockNumbers[i]);
        }

        // 書き込みデータ
        for (int i = 0; i < datas.length; i++) {
            byteBuffer.put(datas[i]);
        }

        byteBuffer.flip();
        byte[] command = new byte[byteBuffer.limit()];
        byteBuffer.get(command, byteBuffer.position(), byteBuffer.limit());

        byte[] response = executeCommand(command);

        return response;
    }

    /**
     * Write Without Encryptionコマンドを発行します<br>
     * FeliCa Liteなので、1度のコマンド発行で1ブロックだけ書き込めます
     *
     * @param idm         IDm
     * @param blockNumber ブロック番号
     * @param data        書き込みデータ
     * @return レスポンス
     * @throws TagLostException
     * @throws IOException
     */
    public byte[] writeWithoutEncryption(byte[] idm, int blockNumber, byte[] data) throws TagLostException, IOException {
        if (idm == null || idm.length == 0) throw new IllegalArgumentException();

        ByteBuffer byteBuffer = ByteBuffer.allocate(31);

        // Write Without Encryption
        byteBuffer.put(WRITE_WITHOUT_ENCRYPTION);

        // IDm
        byteBuffer.put(idm);

        // サービス数
        // FeliCa Liteなので1に固定
        byteBuffer.put((byte) 0x01);

        // サービスコード（リトルエンディアン）
        // 0x00 0x09
        byteBuffer.put((byte) 0x09);
        byteBuffer.put((byte) 0x00);

        // ブロック数
        // FeliCa Liteなので1に固定
        byteBuffer.put((byte) 0x01);

        // ブロックリスト
        // 長さ 2Byteなので1b
        // アクセスモード FeliCa Liteなので000bに固定
        // サービスコード順番 FeliCa Liteなので0000bに固定
        // ブロック番号 引数から指定
        byteBuffer.put((byte) 0x80);
        byteBuffer.put((byte) blockNumber);

        // 書き込みデータ
        byteBuffer.put(data);

        byte[] command = byteBuffer.array();
        byte[] response = executeCommand(command);

        return response;
    }
//	u8 NFC_TAG_TYPE3_BLOCK_WRITE(UART_Handle uart, PIN_Handle out, PIN_Handle in, u8 addr, u8 *data, u8 *buff)
//	{
//	  u8 i;
//	  u8 tmp;
//
//	  TNR_WRITE(out, in, 0x09, 0x01);
//
//
//	  TNR_WRITE(out, in, 0x02, 0x20);
//	  TNR_WRITE(out, in, 0x02, 0x08);
//	  for(i=0;i<8;i++)TNR_WRITE(out, in, 0x02, IDm[i]);
//	  TNR_WRITE(out, in, 0x02, 0x01);
//	  TNR_WRITE(out, in, 0x02, 0x09);//Read/Write
//	  //TNR_WRITE(out, in, 0x02, 0x0B);ptr++;//Read Only
//	  TNR_WRITE(out, in, 0x02, 0x00);
//	  TNR_WRITE(out, in, 0x02, 0x01);
//	  TNR_WRITE(out, in, 0x02, 0x80);
//	  TNR_WRITE(out, in, 0x02, addr);
//	  for(i=0;i<16;i++) TNR_WRITE(out, in, 0x02, data[i]);
//
//	  tmp = TNR_RECEIVE_CHECK(out, in, 20, &buff[0]);
//	  if(tmp == 0) return 0;
//
//	  #if nfc_type3_debug
//	  TNR100_HOST_SEND_STRING(uart, "\r\nNFC Tag Type BLOCK(");TNR100_HOST_SEND_H2A(uart, addr);TNR100_HOST_SEND_STRING(uart, ") WRITE = ");
//	  for(i=0;i<tmp;i++) TNR100_HOST_SEND_H2A(uart, buff[i]);
//	  #endif
//
//	  return tmp;
//	}

    public byte[] readWithoutEncryption(byte[] idm, int blockNumber) throws TagLostException, IOException {
        if (idm == null || idm.length == 0) throw new IllegalArgumentException();

        ByteBuffer byteBuffer = ByteBuffer.allocate(15);

        // Write Without Encryption
        byteBuffer.put(READ_WITHOUT_ENCRYPTION);

        // IDm
        byteBuffer.put(idm);

        // サービス数
        // FeliCa Liteなので1に固定
        byteBuffer.put((byte) 0x01);

        // サービスコード（リトルエンディアン）
        // 0x00 0x09
//		byteBuffer.put((byte) 0x09);//Read/Write
        byteBuffer.put((byte) 0x0B);//Read Only
        byteBuffer.put((byte) 0x00);

        // ブロック数
        // FeliCa Liteなので1に固定
//		byteBuffer.put((byte) 0x01);
        byteBuffer.put((byte) 0x04);

        // ブロックリスト
        // 長さ 2Byteなので1b
        // アクセスモード FeliCa Liteなので000bに固定
        // サービスコード順番 FeliCa Liteなので0000bに固定
        // ブロック番号 引数から指定
        byteBuffer.put((byte) 0x80);
        byteBuffer.put((byte) blockNumber);

        byte[] command = byteBuffer.array();
        byte[] response = executeCommand(command);

        return response;
    }

    public byte[] readWithoutEncryptionN(byte[] idm, int blockNumber, byte blockCnt) throws TagLostException, IOException {
        if (idm == null || idm.length == 0) throw new IllegalArgumentException();

        int chkCnt = ((blockCnt - 1) * 2);
        ByteBuffer byteBuffer = ByteBuffer.allocate(15 + chkCnt);

        // Write Without Encryption
        byteBuffer.put(READ_WITHOUT_ENCRYPTION);

        // IDm
        byteBuffer.put(idm);

        // サービス数
        // FeliCa Liteなので1に固定
        byteBuffer.put((byte) 0x01);

        // サービスコード（リトルエンディアン）
        // 0x00 0x09
//		byteBuffer.put((byte) 0x09);//Read/Write
        byteBuffer.put((byte) 0x0B);//Read Only
        byteBuffer.put((byte) 0x00);

        // ブロック数
        // FeliCa Liteなので1に固定
//		byteBuffer.put((byte) 0x01);
        byteBuffer.put((byte) blockCnt);

        // ブロックリスト
        // 長さ 2Byteなので1b
        // アクセスモード FeliCa Liteなので000bに固定
        // サービスコード順番 FeliCa Liteなので0000bに固定
        // ブロック番号 引数から指定
        for (int i = 0; i < blockCnt; i++) {
            byteBuffer.put((byte) 0x80);
            byteBuffer.put((byte) (blockNumber + i));
        }

        byte[] command = byteBuffer.array();
        byte[] response = executeCommand(command);

        return response;
    }

//	u8 NFC_TAG_TYPE3_MUTIBLOCK_READ(UART_Handle uart, PIN_Handle out, PIN_Handle in, u8 addr, u8 *buff)
//	{
//	  u8 i;
//	  u8 tmp;
//
//	  TNR_WRITE(out, in, 0x09, 0x01);
//
//	  TNR_WRITE(out, in, 0x02, 0x10);
//	  TNR_WRITE(out, in, 0x02, 0x06);
//	  for(i=0;i<8;i++)TNR_WRITE(out, in, 0x02, IDm[i]);
//	  TNR_WRITE(out, in, 0x02, 0x01);
//	  //TNR_WRITE(out, in, 0x02, 0x09);//Read/Write
//	  TNR_WRITE(out, in, 0x02, 0x0B);//Read Only
//	  TNR_WRITE(out, in, 0x02, 0x00);
//	  TNR_WRITE(out, in, 0x02, 0x04);
//	  TNR_WRITE(out, in, 0x02, 0x80);
//	  TNR_WRITE(out, in, 0x02, addr);
//
//	  tmp = TNR_RECEIVE_CHECK(out, in, 50, &buff[0]);
//
//	  if(tmp == 0) return 0;
//
//	  #if nfc_type3_debug
//	  TNR100_HOST_SEND_STRING(uart, "\r\nNFC Tag Type MULTI BLOCK(");TNR100_HOST_SEND_H2A(uart, addr);TNR100_HOST_SEND_STRING(uart, ") READ = ");
//	  for(i=0;i<tmp;i++) TNR100_HOST_SEND_H2A(uart, buff[i]);
//	  #endif
//
//	  return tmp;
//	}

    //	  TRH_WRITE(REG_FIFODATA, 0x10);  //LEN
//	  TRH_WRITE(REG_FIFODATA, 0x06);  //Check Command Code
//
//	  for(i=0; i<8; i++)
//	    TRH_WRITE(REG_FIFODATA, FCID[2+i]);  //IDm[0..7]
//
//	  TRH_WRITE(REG_FIFODATA, 0x01);  //Number of Services
//	  TRH_WRITE(REG_FIFODATA, 0x0B);  //Service Code List0
//	  TRH_WRITE(REG_FIFODATA, 0x00);  //Service Code List1
//	  TRH_WRITE(REG_FIFODATA, 0x01);  //Number of Blocks
//	  TRH_WRITE(REG_FIFODATA, nb_h);  //Block List0(Fixed)
//	  TRH_WRITE(REG_FIFODATA, nb_l);  //Block List1(Block0)
    public byte[] pollingWithoutEncryption() throws TagLostException, IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(5);

        byteBuffer.put(POLLING_WITHOUT_ENCRYPTION);
        byteBuffer.put((byte) 0xFF);
        byteBuffer.put((byte) 0xFF);
        byteBuffer.put((byte) 0x00);
        byteBuffer.put((byte) 0x00);

        byte[] command = byteBuffer.array();
        byte[] response = executeCommand(command);

        return response;
    }

    public byte[] updateATRWithoutEncryption(byte[] idm, byte wr_flag) throws TagLostException, IOException {
        if (idm == null || idm.length == 0) throw new IllegalArgumentException();

        ByteBuffer byteBuffer = ByteBuffer.allocate(31);

        byteBuffer.put(ATR_WITHOUT_ENCRYPTION);

        byteBuffer.put(idm);                            // IDm

        byteBuffer.put((byte) 0x01);                        // Number of Services
        byteBuffer.put((byte) 0x09);                        // Service Code List0(Write)
        byteBuffer.put((byte) 0x00);                        // Service Code List1
        byteBuffer.put((byte) 0x01);                        // Number of Blocks
        byteBuffer.put((byte) 0x80);                        // Block List0(Fixed)
        byteBuffer.put((byte) 0x00);                        // Block List1(Block0)

        short checkSum = 0;
        byteBuffer.put((byte) 0x10);
        checkSum += 0x10;    // Ver1.0
        byteBuffer.put((byte) 0x08);
        checkSum += 0x08;    // Nbr=8
        byteBuffer.put((byte) 0x04);
        checkSum += 0x04;    // Nbw=4
        byteBuffer.put((byte) 0x00);
        checkSum += 0x00;    // Nmaxb0
        byteBuffer.put((byte) 0xFF);
        checkSum += 0xFF;    // Nmaxb1 = 255 blocks(4KB)
        byteBuffer.put((byte) 0x00);
        checkSum += 0x00;    // Unused
        byteBuffer.put((byte) 0x00);
        checkSum += 0x00;    // Unused
        byteBuffer.put((byte) 0x00);
        checkSum += 0x00;    // Unused
        byteBuffer.put((byte) 0x00);
        checkSum += 0x00;    // Unused
        byteBuffer.put(wr_flag);
        checkSum += wr_flag;    // WriteFlag, 0x0F=Writing in progress, 0x00= Write finished
        byteBuffer.put((byte) 0x01);
        checkSum += 0x01;    // RWFlag, R/W available
        byteBuffer.put((byte) 0x00);
        checkSum += 0x00;    // Ln0
        byteBuffer.put((byte) 0x00);
        checkSum += 0x00;    // Ln1
        byteBuffer.put((byte) 0x20);
        checkSum += 0x20;    // Ln2, Ln=0x000020 = 32Bytes
        byteBuffer.put((byte) (checkSum >> 8));            // Checksum0
        byteBuffer.put((byte) checkSum);                    // Checksum1

        byte[] command = byteBuffer.array();
        byte[] response = executeCommand(command);

        return response;
    }

    /**
     * コマンドを実行します<br>
     * 自動的に先頭にコマンド長を付加します
     *
     * @param command
     * @return
     * @throws TagLostException
     * @throws IOException
     */
    public byte[] executeCommand(byte[] command) throws TagLostException, IOException {
        if (command == null || command.length == 0) throw new IllegalArgumentException();

        int commandLength = command.length;
        byte[] rawCommand = new byte[commandLength + 1];
        int rawCommandLength = rawCommand.length;
        rawCommand[0] = (byte) rawCommandLength;
        System.arraycopy(command, 0, rawCommand, 1, commandLength);

        return executeRawCommand(rawCommand);
    }

    /**
     * コマンドを渡された状態そのままで実行します
     *
     * @param rawCommand
     * @return
     * @throws TagLostException
     * @throws IOException
     */
    public byte[] executeRawCommand(byte[] rawCommand) throws TagLostException, IOException {
        if (rawCommand == null || rawCommand.length == 0) throw new IllegalArgumentException();

        try {
            mNfcF.connect();
            byte[] response = mNfcF.transceive(rawCommand);
//			if(D) Log.d(TAG, "executeRawCommand 03");
            return response;
        } finally {
            try {
                mNfcF.close();
            } catch (IOException e) {
                //Log.w(TAG, "executeRawCommand fail!", e);
                if (D) Log.d(TAG, "executeRawCommand fail!", e);
            }
        }
    }

    /**
     * 対応していないタグを渡された際に発生する例外です
     *
     * @author tomorrowkey@gmail.com
     */
    public static class UnsupportTagException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    /**
     * タグに書き込めるサイズを越えた際に発生する例外です
     *
     * @author tomorrowkey@gmail.com
     */
    public static class SizeOverflowException extends Exception {

        private static final long serialVersionUID = 1L;

        private int mSize;

        private int mCapacitySize;

        public SizeOverflowException(int size, int capacitySize) {
            mSize = size;
            mCapacitySize = capacitySize;
        }

        @Override
        public String getMessage() {
            return "size over, size=" + mSize + ", capacity=" + mCapacitySize;
        }

        public int getSize() {
            return mSize;
        }

        public int getCapacity() {
            return mCapacitySize;
        }
    }
}
