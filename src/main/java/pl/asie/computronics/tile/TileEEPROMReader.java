package pl.asie.computronics.tile;

import cpw.mods.fml.common.Optional;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import pl.asie.computronics.reference.Mods;
import pl.asie.lib.api.tile.IInventoryProvider;

public class TileEEPROMReader extends TileEntityPeripheralBase implements IInventoryProvider {
	public TileEEPROMReader() {
		super("eeprom_reader");
		this.createInventory(1);
	}

	@Override
	public void onSlotUpdate(int slot) {
		int meta = this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		int newMeta = -1;
		if(isReady() && meta == 0) newMeta = 1;
		else if(!isReady() && meta == 1) newMeta = 0;
		if(newMeta >= 0) this.worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, newMeta, 2);
	}

	public boolean isReady() {
    	ItemStack is = this.getStackInSlot(0);
    	return is != null && is.stackSize > 0;
	}
	
	public byte[] getData() {
    	ItemStack is = this.getStackInSlot(0);
		if(!isReady()) {
			return new byte[0];
		} else {
			if(!is.hasTagCompound()){
				is.setTagCompound(new NBTTagCompound());
			}
			return is.getTagCompound().getByteArray("ram");
		}
	}
	
	public int getSize() { return getData().length; }
	
	public void setData(byte[] data) {
    	ItemStack is = this.getStackInSlot(0);
		if(!isReady()) return;
		if(!is.hasTagCompound()){
			is.setTagCompound(new NBTTagCompound());
		}
		is.getTagCompound().setByteArray("ram", data);
	}
	
    @Callback(doc = "function():boolean; Returns whether the reader has a valid EEPROM in it",direct = true)
    @Optional.Method(modid=Mods.OpenComputers)
    public Object[] isReady(Context context, Arguments args) {
		return new Object[]{ isReady() };
    }
    
    @Callback(doc = "function():number; Returns the size of the data on the current EEPROM", direct = true)
    @Optional.Method(modid=Mods.OpenComputers)
    public Object[] getSize(Context context, Arguments args) {
		return new Object[]{ getSize() };
    }
    
    @Callback(doc = "function(pos:number [, length:number]):number or string; Reads the piece of data at the specified position, with optionally the specified length", direct = true)
    @Optional.Method(modid=Mods.OpenComputers)
    public Object[] read(Context context, Arguments args) {
    	byte[] data = getData();
		if(args.count() >= 1 && args.isInteger(0)) {
			int pos = args.checkInteger(0);
			if(pos < 0 || pos >= data.length) return null;
			if(args.count() >= 2 && args.isInteger(1)) {
				int len = args.checkInteger(1);
				if(pos+len >= data.length) return null;
				byte[] out = new byte[len];
				System.arraycopy(data, pos, out, 0, len);
				return new Object[]{out};
			} else return new Object[]{(int)data[pos]};
		} else return null;
    }

    @Callback(doc = "function(pos:number, data:number or string):boolean; Writes the specified data to the specified position. Returns true on success", direct = true)
    @Optional.Method(modid=Mods.OpenComputers)
    public Object[] write(Context context, Arguments args) {
    	byte[] data = getData();
    	if(args.count() == 2 && args.isInteger(0)) {
			int pos = args.checkInteger(0);
			if(pos < 0 || pos >= data.length) return new Object[]{false};
    		if(args.isByteArray(1)) {
    			byte[] inject = args.checkByteArray(1);
    			if(pos+inject.length >= data.length) return new Object[]{false};
    			System.arraycopy(inject, 0, data, pos, inject.length);
    		} else if(args.isInteger(1)) {
    			data[pos] = (byte)args.checkInteger(1);
    		} else return new Object[]{false};
    		
    		setData(data);
    		return new Object[]{true};
    	} else return null;
    }
    
	@Override
    @Optional.Method(modid=Mods.ComputerCraft)
	public String[] getMethodNames() {
		return new String[]{"isReady", "getSize", "read", "write"};
	}

	@Override
    @Optional.Method(modid=Mods.ComputerCraft)
	public Object[] callMethod(IComputerAccess computer, ILuaContext context,
			int method, Object[] arguments) throws LuaException,
			InterruptedException {
		switch(method) {
		case 0: return new Object[] { isReady() };
		case 1: return new Object[] { getSize() };
		}
		// 2 or 3
		if(arguments.length == 0 || !(arguments[0] instanceof Double)) return null;
		byte[] data = getData();
		int pos = ((Double)arguments[0]).intValue();
		if(pos < 0 || pos >= data.length) return null;
		switch(method) {
		case 2: return new Object[] { (int)data[pos] };
		case 3: {
			if(arguments.length >= 2 && (arguments[1] instanceof Double)) {
				data[pos] = (byte)(((Double)arguments[1]).intValue());
				setData(data);
			}
		}
		}
		return null;
	}
	
	private short _nedo_addr;
	@Override
	public short busRead(int addr) {
		switch((addr & 0xFFFE)) {
		case 0: return (short)(getData().length >> 1);
		}
		return 0;
	}

	@Override
	public void busWrite(int addr, short data) {

	}
}
