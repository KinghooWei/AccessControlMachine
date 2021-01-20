package com.serialport;

public class yzrfidAPI {
	//******** 功能：获得动态库版本号 *******************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        Ver：返回的版本号，占用4个字节
	//  返回：成功则返回1
	//******************************************************/  
    public native int rfLibVer(int fd,int nDevice,byte Ver[]);
    
	//******** 功能：打开关闭电源*******************/
	//  参数：sPowerDev：  设备描述符
	//        nOnOff：  =1 打开电源   =0 关闭电源
	//  返回：成功则返回1
	//******************************************************/  
    
	public native int rfPower(String sPowerDev, int nOnOff);  
    
	//******** 功能：初始化串口 *******************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符=0
	//        sPort：串口字符串
    //        nBaud：波特率  （19200，38400，115200）
    //               注意：模块或者读卡器上电默认波特率应为这3种的一种
	//  返回：成功则返回1
	//******************************************************/  
    
	public native int rfInitCom(int nDevice,String sPort, int nBaud);  
	
	//******** 功能：关闭串口 *******************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符=0
	//  返回：成功则返回1
	//*******************************************************/  
	public native int rfClosePort(int fd,int nDevice);	
	
	//******** 功能：控制读卡器天线 *******************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        mode：  =0：关闭 
  	//               =1：打开
	//  返回：成功则返回1
	//*********************************************************/
	public native int rfAntennaSta(int fd,int nDevice, byte mode);
	
	//******** 功能：设置读写卡器非接触工作方式为  *******************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//  说明：type='A':设置为TYPE_A方式
	//        type='B':设置为TYPE_B方式
	//        type='r':设置为AT88RF020卡方式
	//        type='s':设置为SR176,SRI512,SRI4K方式
	//        type='C':设置为Felica方式
	//        type='1':设置(数字1)为ISO15693方式
	//  返回：成功则返回1
	//*********************************************************/
	public native int rfInitType(int fd,int nDevice, byte type);	
			
	//******** 功能：寻ISO14443-3 TYPE_A 卡 *******************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        model：  寻卡模式
	//        TagType：返回卡类型值
	//  返回：成功则返回1
	//  说明：mode=0x26:寻未进入休眠状态的卡
	//        mode=0x52:寻所有状态的卡
	//*******************************************************/
	public native int rfRequest(int fd,int nDevice, byte mode, byte TagType[]);
	
	//******** 功能：寻ISO14443-3 TYPE_A 卡防冲突 *******************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        bcnt：   卡序列号字节数，取值4、7、10，此参数目前不用再管，随意输入一个数即可
	//        pSnr：   返回的卡序列号
	//		  pRLength:卡序列号长度
	//  返回：成功则返回1
	//*******************************************************/
	public native int rfAnticoll(int fd,int nDevice, byte bcnt, byte pSnr[],byte pRLength[]);	
	
	
	//******** 功能：锁定一张ISO14443-3 TYPE_A 卡 *************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        pSnr： 卡序列号
	//        srcLen:卡序列号长度，MifareOne卡该值等于4
	//		  Size： 返回卡容量
	//  返回：成功则返回1
	//*********************************************************/
	public native int rfSelect(int fd,int nDevice, byte pSnr[],byte srcLen, byte Size[]);	

	//******* 功能：命令已激活的ISO14443-3 TYPE_A卡进入休眠状态*/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//  返回：成功则返回1
	//**********************************************************/
	public native int rfHalt(int fd,int nDevice);

	
	
	//*******  功能：密钥认证 ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        mode:  0x60 A密钥，0x61 B密钥
	//        block： M1卡绝对块号
	//        key： 6字节密钥
	//        //pLen:   读出数据的长度
	//  返回：成功则返回1
	//*********************************************************/
	public native int rfM1Authentication2(int fd,int nDevice,byte mode,byte block,byte key[]/*,byte KeyLen*/);
	
	//*******  功能：读取Mifare One 卡一块数据 ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        adr： M1卡绝对块号
	//        pData： 读出数据
	//        pLen:   读出数据的长度
	//  返回：成功则返回1
	//*********************************************************/
	public native int rfM1read(int fd,int nDevice, byte adr, byte pData[],byte pLen[]);
	
	//*******  功能：向Mifare One 卡中写入一块数据 ************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        adr：M1卡绝对块号
	//        data： 写入的数据，16 字节
	//  返回：成功则返回1
	//*********************************************************/
	public native int rfM1Write(int fd,int nDevice,byte adr, byte data[]/*,unsigned char len*/);

	//*******  功能：将Mifare One 卡某一扇区初始化为钱包 *******/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        adr：M1 卡块地址
	//        value：初始值（4字节，低字节在前）
	//  返回：成功则返回1
	//*********************************************************/
	public native int rfM1InitVal(int fd,int nDevice,byte adr, byte value[]);
	
	//*******  功能：读Mifare One 钱包值 **********************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        adr： M1 卡块地址
	//        pValue：返回的值（4字节，低字节在前）
	//  返回：成功则返回1
	//*********************************************************/
	public native int rfM1ReadVal(int fd,int nDevice, byte adr,byte pValue[]);	
	
	//*******  功能：Mifare One 钱包扣款 **********************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        adr： M1 卡块地址
	//         value：要扣的值（4字节，低字节在前）
	//返回：成功则返回1
	//*********************************************************/
	public native int rfM1Decrement(int fd,int nDevice,byte adr,byte pValue[]);
	
	//*******  功能：Mifare One 钱包充值 **********************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        adr： M1 卡块地址
	//         value：要充的值（4字节，低字节在前）
	//返回：成功则返回1
	//*********************************************************/
	public native int rfM1Increment(int fd,int nDevice,byte adr,byte pValue[]);	

	//*******  功能：Mifare One 卡值回传 **********************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        adr： M1 卡块地址
	//  说明：用此函数将指定的块内容传入卡的buffer，然后可用
	//  rf_M1transfer()函数将buffer 中数据再传送到另一块中去
	//返回：成功则返回1
	//*********************************************************/
	public native int rfM1Restore(int fd,int nDevice, byte adr);

	//*******  功能：将Mifare One数据传送  **********************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        adr： M1 卡块地址
	//说明：该函数仅在restore 命令之后调用。
	//返回：成功则返回1
	//*********************************************************/
	public native int rfM1Transfer(int fd,int nDevice,byte adr);
	
	//*******  功能：下载密钥到读卡模块中（需要确定型号是否支持）  **********************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//			Mode：密钥类型,取值‘A’OR ‘B’
	//			key：密钥，96 字节，每6 个字节为1 个密钥，0～15 扇区顺序排列
	//返回：成功则返回1
	//注：//未测试函数，如有问题请联系圆志
	//*********************************************************/
	public native int rfDownloadKey(int fd,int nDevice,byte mode,byte key[]);		
	
	//*******  功能：用已下载密钥认证读卡模块中（需要确定型号是否支持）  **********************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//  		model：密码验证模式,取值‘A’OR ‘B’
	//  		secnr：要验证密码的扇区号
	//返回：成功则返回1
	//注：//未测试函数，如有问题请联系圆志
	//*********************************************************/
	public native int rfM1Authentication1(int fd,int nDevice,byte mode,byte secnr);		

	//*******  功能：下载密钥到M104EPC读卡模块中  **********************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//  cSectorNo：扇区号0-39
	//  key：12字节密钥，前6字节A密钥+后6字节B密钥
	//返回：成功则返回1
	//注：//未测试函数，如有问题请联系圆志
	//*********************************************************/
	public native int rfDownloadKey2(int fd,int nDevice,byte cSectorNo,byte key[]);	
	
	//*******  功能：用已下载密钥认证M104EPC读卡模块  **********************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//  		mode：mode=0x60 认证A密钥
	//  			mode=0x61 认证B密钥
	//			secnr：要认证的扇区号
	//返回：成功则返回1
	//注：//未测试函数，如有问题请联系圆志
	//*********************************************************/
	public native int rfM1Authentication3(int fd,int nDevice,byte mode,byte secnr);		


	
	//******** 功能：LED指示灯（需要硬件支持）*******************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        color：   =0 ：LED1熄灯
	//                =1 ：LED2熄灯
	//                =2 ：LED2点亮
	//                =3 ：LED1点亮
	//  返回：成功则返回1
	//*********************************************************/
	public native int rfLight(int fd,int nDevice, byte color);
	
	//******** 功能：控制蜂鸣器 *******************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        msec：   蜂鸣时间（00-FF）
	//  返回：成功则返回1
	//*********************************************************/
	public native int rfBeep(int fd,int nDevice, byte msec);
	
	//*******  功能：复位TypeA CPU卡 ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        model：  寻卡模式
	//        pData：返回的复位信息
	//        pMsgLg：返回的长度
	//  返回：成功则返回1
	//  说明：model=0x26:寻未进入休眠状态的卡
	//        model=0x52:寻所有状态的卡
	//*********************************************************/
	public native int rfTypeARst(int fd,int nDevice, byte model, byte pData[],byte pMsgLg[]);

	//*******  功能：向符合ISO14443-A 标准的CPU 卡发送COS 命令  ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        command：  COS命令
	//        cmdLen：COS命令长度
	//        pData：卡片返回的数据，含SW1、SW2
	//        pMsgLg：返回的长度
	//  返回：成功则返回1
	//*********************************************************/	
	public native int rfCosCommand(int fd,int nDevice, byte command[],byte cmdLen,byte pData[],byte pMsgLg[]);
	
	//*******  功能：SAM初始化 ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        bound：  sam 卡波特率，取值为0表示9600，取值为1表示38400
	//        pData：返回的复位信息
	//        pMsgLg：返回的长度
	//  返回：成功则返回1
	//*********************************************************/	
	public native int rfInitSam(int fd,int nDevice, byte bound);	

	//*******  功能：复位sam 卡 ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        pData：返回的复位信息
	//        pMsgLg：返回的长度
	//  返回：成功则返回1
	//*********************************************************/
	public native int rfSamRst(int fd,int nDevice,byte pData[],byte pMsgLg[]);
	
	//*******  功能：向SAM 卡发送COS 命令  ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        command：  COS命令
	//        cmdLen：COS命令长度
	//        pData：卡片返回的数据，含SW1、SW2
	//        pMsgLg：返回的长度
	//  返回：成功则返回1
	//*********************************************************/	
	public native int rfSamCos(int fd,int nDevice, byte command[],byte cmdLen,byte pData[],byte pMsgLg[]);	
	
	//*******  功能：复位带几个卡座的sam 卡  ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        cSetting：输入
	//                bit1-bit0波特率选择：00表示复位9600的卡；01表示复位38400的卡
	//		          bit3-bit2改变选中卡状态：00表示复位操作；10表示升频到38400（前提是SAM卡要
	//                         支持从9600到38400的跳变）	
	//                bit7-bit6-bit5-bit4选择SAM卡： 0000：表示1号卡；0001：表示2号卡
	//                          0010：表示3号卡；0011：表示4号卡；0100：表示5号卡
	//        cSetLen：输入的长度，对于有个模块支持功能强的，长度字不是1个字节，请根据模块来定
	//        pData： 返回的复位信息内容
	//        pMsgLg：返回复位信息的长度
	//  返回：成功则返回1
	//*********************************************************/	
	public native int rfSamRst2(int fd,int nDevice, byte cSetting[],byte cSetLen,byte pData[],byte pMsgLg[]);		
	
	//*******  功能：向带几个SAM 卡座的SAM卡发送COS 命令  ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//		  SAMNo:   SAM卡编号
	//        command：cos 命令
	//        cmdLen:  cos 命令长度
	//        pDate：  卡片返回的数据，含SW1、SW2
	//        pMsgLg： 返回数据长度
	//  返回：成功则返回1
	//注：//未测试函数，如有问题请联系圆志
	//*********************************************************/	
	public native int rfSamCos2(int fd,int nDevice,byte cSAMNo,byte command[],byte cmdLen,byte pDataReturn[],byte pMsgLg[]);	
	
	//*******  功能：锁定一张ultra light卡  ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//		  pSnr： 卡序列号
	//        pLen：序列号长度
	//  返回：成功则返回1
	//注：//未测试函数，如有问题请联系圆志
	//*********************************************************/	
	public native int rfULSelect(int fd,int nDevice,byte pSnr[],byte pLen[]);
	
	//*******  功能：向ultra light卡中写入一块数据   ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//		  adr：  ultra light卡页地址（0～0x0f）
	//        data：  写入的数据，4字节
	//  返回：成功则返回1
	//注：//未测试函数，如有问题请联系圆志
	//*********************************************************/	
	public native int rfULWrite(int fd,int nDevice,byte adr,byte data[]);	
	
	//*******  功能：ISO15693_Inventory  ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//		  Pdata: 返回的数据，1字节DSFID+8字节UID
	//        pLen:	 Pdata长度
	//  返回：成功则返回1
	//*********************************************************/	
	public native int ISO15693Inventory(int fd,int nDevice,byte pData[],byte pLen[]);
	
	//*******  功能：ISO15693_Stay_Quiet  ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//		  UID:	 UID 8字节
	//  返回：成功则返回1
	//*********************************************************/	
	public native int ISO15693StayQuiet(int fd,int nDevice,byte UID[]);
	
	//*******  功能：ISO15693_Select  ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//		  UID:	 UID 8字节
	//  返回：成功则返回1
	//*********************************************************/		
	public native int ISO15693Select(int fd,int nDevice,byte UID[]);

	//*******  功能：ISO15693_Reset_To_Ready   ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        model: bit0=Select_flags,bit1=Addres_flags
	//		  UID:	 UID 8字节
	//  返回：成功则返回1
	//*********************************************************/	
	public native int ISO15693ResetToReady(int fd,int nDevice,byte model,byte UID[]);
	
	//*******  功能：ISO15693_Read   ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        model: bit0=Select_flags,bit1=Addres_flags
	//  		UID:	 UID 8字节
	//  		block: 块号
	//  		number:要读取的块数，< 0x10
	//  		pDataReturn: 返回的数据
	//  		pLen:  返回数据的长度
	//  返回：成功则返回1
	//*********************************************************/
	public native int ISO15693Read(int fd,int nDevice,
			                                byte model,
			                                byte UID[],
			                                byte block,
			                                byte number,
			                                byte pDataReturn[],
			                                byte pLen[]);

	//*******  功能：ISO15693_Write   ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        model: bit0=Select_flags,bit1=Addres_flags
	//  		UID:	 UID 8字节
	//  		block: 块号
	//  		data:  要写入的数据，4字节
	//  返回：成功则返回1
	//*********************************************************/
	public native int ISO15693Write(int fd,int nDevice,
			                                byte model,
			                                byte UID[],
			                                byte block,
			                                byte data[]);	

	//*******  功能：ISO15693_Lock_Block   ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        model: bit0=Select_flags,bit1=Addres_flags
	//  		UID:	 UID 8字节
	//  		block: 块号
	//  返回：成功则返回1
	//*********************************************************/
	public native int ISO15693LockBlock(int fd,int nDevice,
							            byte model,
							            byte UID[],
							            byte block);	
	//*******  功能：ISO15693_Write_AFI   ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        model: bit0=Select_flags,bit1=Addres_flags
	//  		UID:	 UID 8字节
	//  		AFI:   要写入的AFI
	//  返回：成功则返回1
	//*********************************************************/
	public native int ISO15693WriteAFI(int fd,int nDevice,
							            byte model,
							            byte UID[],
							            byte AFI);	
	
	//*******  功能：ISO15693_Lock_AFI   ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        model: bit0=Select_flags,bit1=Addres_flags
	//  		UID:	 UID 8字节
	//  返回：成功则返回1
	//*********************************************************/
	public native int ISO15693LockAFI(int fd,int nDevice,
							            byte model,
							            byte UID[]);	
		
	//*******  功能：ISO15693_Write_DSFID   ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        model: bit0=Select_flags,bit1=Addres_flags
	//  		UID:	 UID 8字节
	//  		DSFID:   要写入的DSFID
	//  返回：成功则返回1
	//*********************************************************/
	public native int ISO15693WriteDSFID(int fd,int nDevice,
							            byte model,
							            byte UID[],
							            byte DSFID);	
	
	//*******  功能：ISO15693_Lock_DSFID   ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        model: bit0=Select_flags,bit1=Addres_flags
	//  		UID:	 UID 8字节
	//  返回：成功则返回1
	//*********************************************************/
	public native int ISO15693LockDSFID(int fd,int nDevice,
							            byte model,
							            byte UID[]);

	//*******  功能：ISO15693_Get_System_Information   ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        model: bit0=Select_flags,bit1=Addres_flags
	//  		UID:	 UID 8字节
	//  		Pdata: 返回的数据1字节信息标志+8字节UID+1字节DSFID+1字节AFI+信息域
	//  		pLen:  返回数据的长度
	//  返回：成功则返回1
	//*********************************************************/
	public native int ISO15693GetSystemInformation(int fd,int nDevice,
			                                byte model,
			                                byte UID[],
			                                byte pDataReturn[],
			                                byte pLen[]);
	
	
	//*******  功能：ISO15693_Get_Block_Security   ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        model: bit0=Select_flags,bit1=Addres_flags
	//  		UID:	 UID 8字节
	//			block:	 块号
	//  		number:要读取的块数，< 0x40
	//  		Pdata: 返回的数据
	//  		pLen:  返回数据的长度
	//  返回：成功则返回1
	//*********************************************************/
	public native int ISO15693GetBlockSecurity(int fd,int nDevice,
			                                byte model,
			                                byte UID[],
			         					    byte  block,
			        					    byte  number, 
			                                byte pDataReturn[],
			                                byte pLen[]);	
	
	//*******  功能：ISO15693_SET_EAS_ICODE   ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        model: bit0=Select_flags,bit1=Addres_flags
	//  		UID:	 UID 8字节
	//  返回：成功则返回1
	//*********************************************************/
	public native int ISO15693SetEasIcode(int fd,int nDevice,
							            byte model,
							            byte UID[]);
	
	//*******  功能：ISO15693_RESET_EAS_ICODE   ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        model: bit0=Select_flags,bit1=Addres_flags
	//  		UID:	 UID 8字节
	//  返回：成功则返回1
	//*********************************************************/
	public native int ISO15693ResetEasIcode(int fd,int nDevice,
							            byte model,
							            byte UID[]);	
	
	//*******  功能：ISO15693_LOCK_EAS_ICODE   ****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        model: bit0=Select_flags,bit1=Addres_flags
	//  		UID:	 UID 8字节
	//  返回：成功则返回1
	//*********************************************************/
	public native int ISO15693LockEasIcode(int fd,int nDevice,
							            byte model,
							            byte UID[]);	
	
	//******** 功能：ISO15693_EAS_ALARM_ICODE ***************************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        model: bit0=Select_flags,bit1=Addres_flags
	//        UID:	 UID 8字节
	//        Pdata: 返回的数据
	//        pLen:  返回数据的长度
	//  返回：成功则返回1
	//*********************************************************/
	public native int ISO15693EasAlarmIcode(int fd,int nDevice,
									            byte model,
									            byte UID[],
									            byte pDataReturn[],
									            byte pLen[]);	
	
	//******** 功能：获取二代身份证UID****************************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        model: model=0
	//        Pdata: 返回的数据
	//        pLen:  返回数据的长度
	//  返回：成功则返回1
	//*********************************************************/
	public native int rfGetNameCardUid(int fd,int nDevice,
									            byte model,
									            byte pData[],
									            byte pLen[]);	
	
	//******** 功能：获取Felica UID***************************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        Pdata: 返回的数据
	//        pLen:  返回数据的长度
	//  返回：成功则返回1
	//*********************************************************/
	public native int rfFelicaGetUid(int fd,int nDevice,
									            byte pUID[],
									            byte pLen[]);	
	
	//******** 功能：寻符合ISO14443-B 标准的卡****************************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        model: model=0 寻未进入休眠状态的卡，model= 1：寻所有状态的卡
	//        pData: 返回的数据
	//        pLen:  返回数据的长度
	//  返回：成功则返回1
	//注：//未测试函数，如有问题请联系圆志
	//*********************************************************/
	public native int rfAtqb(int fd,int nDevice,
					            byte model,
					            byte pData[],
					            byte pLen[]);		

	
	//******** 功能：激活已寻到的符合ISO14443-B 标准的卡****************************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//        PUPI：   [IN] 卡片PUPI
	//        CID：   [IN] 卡片CID(0-14)
	//  返回：成功则返回1
	//注：//未测试函数，如有问题请联系圆志
	//*********************************************************/
	public native int rfAttrib(int fd,int nDevice,
						            byte PUPI[],
						            byte CID);	
	
	
	//******** 功能：命令一选中的TYPE_B 卡进入HALT 状态****************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//			PUPI： 卡片唯一标识符
	//返回：成功则返回1
	//注：//未测试函数，如有问题请联系圆志
	/*****************************************************************/
	public native int rfHltb(int fd,int nDevice,byte PUPI[]);

	//*********  功能：向符合ISO14443-B 标准的CPU 卡发送COS 命令 ******/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//CID： 卡片逻辑地址
	//command：cos 命令
	//cmdLen：COS命令长度
	//pData：卡片返回的数据，含SW1、SW2
	//pMsgLg：返回数据长度
	//返回：成功则返回1
	//注：//未测试函数，如有问题请联系圆志
	//*****************************************************************/
	public native int rfTypeBCos(int fd,int nDevice,byte CID,byte command[],byte cmdLen,byte pData[],byte pMsgLg);
	
	//*********  功能：取消锁定TYPE B卡 ******/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//返回：成功则返回1
	//注：//未测试函数，如有问题请联系圆志
	//*****************************************************************/
	public native int rfClDeselect(int fd,int nDevice);

	
	//*********  功能：锁定一张ST卡 ******/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	// 			Chip_ID：卡ID号
	//返回：成功则返回1
	//注：//未测试函数，如有问题请联系圆志
	//*****************************************************************/
	public native int rfStSelect(int fd,int nDevice,byte pChip_ID[]);	
	
	//*********  功能：命令ST卡进入DESACTIVED状态 ******/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//返回：成功则返回1
	//注：//未测试函数，如有问题请联系圆志
	//*****************************************************************/
	public native int rfStCompletion(int fd,int nDevice);

	//*********  功能：读SRIX4K卡1块数据 * ******/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//  		block： 块地址
	//  		pData:  读出的数据
	//  		pLen:	  读出数据的长度	
	//返回：成功则返回1
	//注：//未测试函数，如有问题请联系圆志
	//*****************************************************************/
	public native int rfSrix4kReadBlock(int fd,int nDevice, byte block, byte pData[],byte pLen[]);

	
	//*********  功能：写SRIX4K卡1块数据 * ******/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//  		block： 块地址
	//  		pData:  要写入的数据，4字节
	//返回：成功则返回1
	//注：//未测试函数，如有问题请联系圆志
	//*****************************************************************/
	public native int rfSrix4kWriteBlock(int fd,int nDevice, byte block, byte data[]);

	//*********  功能：获取SRIX4K卡UID * ******/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//  		pUID:	 UID
	//  		pLen:	  UID长度	
	//返回：成功则返回1
	//注：//未测试函数，如有问题请联系圆志
	//*****************************************************************/
	public native int rfSrix4kGetUid(int fd,int nDevice, byte pUID[],byte pLen[]);		

	//*********  功能：SRIX4K卡块锁定  * ******/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//  		lockreg: 1字节块锁定值
	//返回：成功则返回1
	//注：//未测试函数，如有问题请联系圆志
	//*****************************************************************/
	public native int rfSrix4kWriteLockReg(int fd,int nDevice, byte lockreg);
	
	//*********  功能：SRI512卡块锁定  * ******/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//  		lockreg: 2字节LOCKREG+1字节0+1字节Chip_ID,共4个字节
	//返回：成功则返回1
	//注：//未测试函数，如有问题请联系圆志
	//*****************************************************************/
	public native int rfSri512WriteLockReg(int fd,int nDevice, byte lockreg[]);
	
	//*********  功能：NFC type2标签选择扇区*  * ******/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//  		cSector：00-FEh扇区号
	//返回：成功则返回1
	//注：//未测试函数，如有问题请联系圆志
	//*****************************************************************/
	public native int rfNFCSelectSector(int fd,int nDevice, byte cSector);
	
	//*********  功能：Ntag213,215,216标签读取版本号* ******/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//			pVer：返回数据
	//			pLen：返回数据长度
	//返回：成功则返回1
	//注：//未测试函数，如有问题请联系圆志
	//*****************************************************************/
	public native int rfNtagGetVersion(int fd,int nDevice, byte pVer[],byte pLen[]);
	
	//*********  功能：Ntag213,215,216快速读取* ******/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//		cStartAdr：开始地址
	//		cEndAdr：结束地址，目前地址不能超过16个块，稍后确定具体数量
	//		pDataReturn：返回数据
	//		pLen：返回数据长度
	//返回：成功则返回1
	//注：//未测试函数，如有问题请联系圆志
	//*****************************************************************/
	public native int rfNtagFastRead(int fd,int nDevice, byte cStartAdr,byte cEndAdr,byte pDataReturn[],byte pLen[]);	
	
	//*********  功能：Ntag213,215,216标签读取计数值* ******/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//			pData：返回数据
	//			pLen：返回数据长度,成功返回3字节
	//返回：成功则返回1
	//注：//未测试函数，如有问题请联系圆志
	//*****************************************************************/
	public native int rfNtagReadCNT(int fd,int nDevice, byte pData[],byte pLen[]);
	
	//*********  功能：Ntag213,215,216标签密码验证* ******/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//          cPassword：输入密码
	//			pData：返回数据
	//			pLen：返回数据长度,成功返回2字节
	//返回：成功则返回1
	//注：//未测试函数，如有问题请联系圆志
	//*****************************************************************/
	public native int rfNtagPasswordAuth(int fd,int nDevice,byte cPassword[],byte pData[],byte pLen[]);

	//******** 功能：Ntag213,215,216标签读取数字签名******************************/
	//  参数：fd：  串口句柄
	//        nDevice：  通讯设备标识符
	//		pData：返回数据
	//		pLen：返回数据长度,成功返回32字节
	//返回：成功则返回1
	//注：//未测试函数，如有问题请联系圆志
	//********************************************************************/
	public native int rfNtagReadSignature(int fd,int nDevice,byte pData[],byte pLen[]);
	
	
	
	//******** 功能：DES 算法加密函数 *************************/
	//  参数：szOut:  加密结果，长度等于加密数据长度
	//        szIn:   加密数据
	//        inlen:  加密数据长度,8字节的整数倍
	//        key:    密钥
	//        keylen: 密钥长度,如果大于8字节，是3des,如果小于等于8字节单des.不足补零
	//  返回: 成功返回1
	//注：//未测试函数，如有问题请联系圆志
	//*********************************************************/
	public native int desEncrypt(byte szOut[],byte szIn[],int inlen,byte key[],int keylen);

	//******** 功能：DES 算法解密函数 *************************/
	//  参数：szOut:  加密结果，长度等于加密数据长度
	//        szIn:   加密数据
	//        inlen:  加密数据长度,8字节的整数倍
	//        key:    密钥
	//        keylen: 密钥长度,如果大于8字节，是3des,如果小于等于8字节单des.不足补零
	//  返回: 成功返回1
	//注：//未测试函数，如有问题请联系圆志
	//*********************************************************/
	public native int desDecrypt(byte szOut[],byte szIn[],int inlen,byte key[],int keylen);	
	
	
	//******** 功能：MAC 计算 *************************/
	//  参数：szOut:  4字节MAC计算结果
	//        szIn:   输入数据
	//        inlen:  输入数据长度
	//        key:    密钥
	//        keylen: 密钥长度,如果大于8字节，是3des,如果小于等于8字节单des.不足补零
	//        InitVal：MAC计算初始值
	//        Initlen：MAC计算初始长度
	//  返回: 成功返回1
	//注：//未测试函数，如有问题请联系圆志
	//*********************************************************/
	public native int CalMAC(byte szOut[],byte szIn[],int inlen,byte key[],int keylen,byte InitVal[],int Initlen);	
	
	
	static {
        System.loadLibrary("yzrfidAPI");
    }
}
