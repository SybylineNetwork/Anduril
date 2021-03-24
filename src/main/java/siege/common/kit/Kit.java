package siege.common.kit;

import java.util.*;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.common.util.Constants;

public class Kit
{
	
	private static final EquipmentSlotType[] armors = {
		EquipmentSlotType.HEAD,
		EquipmentSlotType.CHEST,
		EquipmentSlotType.LEGS,
		EquipmentSlotType.FEET
	};
	
	private boolean needsSave = false;
	private boolean deleted = false;
	
	private UUID kitID;
	private String kitName;
	
	private ItemStack[] armorItems = {
		ItemStack.EMPTY,
		ItemStack.EMPTY,
		ItemStack.EMPTY,
		ItemStack.EMPTY,
	};
	private ItemStack heldItem = ItemStack.EMPTY;
	private ItemStack offhandItem = ItemStack.EMPTY;
	private List<ItemStack> otherItems = new ArrayList<>();
	private List<EffectInstance> potionEffects = new ArrayList<>();
	
	public Kit()
	{
		kitID = UUID.randomUUID();
	}
	
	public UUID getKitID()
	{
		return kitID;
	}
	
	public String getKitName()
	{
		return kitName;
	}
	
	public void rename(String s)
	{
		String oldName = kitName;
		kitName = s;
		markDirty();
		KitDatabase.renameKit(this, oldName);
	}
	
	public void applyTo(PlayerEntity entityplayer)
	{
		clearPlayerInvAndKit(entityplayer);
		
		for (int i = 0; i < 4; i++)
		{
			ItemStack armor = armorItems[i].copy();
			if (!armor.isEmpty())
			{
				entityplayer.setItemStackToSlot(armors[i], armor);
			}
		}
		
		entityplayer.setItemStackToSlot(EquipmentSlotType.MAINHAND, heldItem.copy());
		entityplayer.setItemStackToSlot(EquipmentSlotType.OFFHAND, offhandItem.copy());
		
		for (ItemStack itemstack : otherItems)
		{
			entityplayer.inventory.addItemStackToInventory(itemstack.copy());
		}
		
		entityplayer.clearActivePotions();
		for (EffectInstance potion : potionEffects)
		{
			EffectInstance copy = new EffectInstance(potion);
			entityplayer.addPotionEffect(copy);
		}
	}
	
	public void createFrom(PlayerEntity entityplayer)
	{
		Arrays.fill(armorItems, ItemStack.EMPTY);
		for (int i = 0; i < 4; i++)
		{
			ItemStack armor = entityplayer.getItemStackFromSlot(armors[i]);
			armorItems[i] = armor.copy();
		}
		
		heldItem = ItemStack.EMPTY;
		offhandItem = entityplayer.inventory.offHandInventory.get(0).copy();
		otherItems.clear();
		for (int i = 0; i < entityplayer.inventory.mainInventory.size(); i++)
		{
			ItemStack itemstack = entityplayer.inventory.mainInventory.get(i);
			if (i == entityplayer.inventory.currentItem)
			{
				heldItem = itemstack.copy();
			}
			else
			{
				if (!itemstack.isEmpty())
				{
					otherItems.add(itemstack.copy());
				}
			}
		}
		
		potionEffects.clear();
		for (EffectInstance potion : entityplayer.getActivePotionEffects())
		{
			EffectInstance copy = new EffectInstance(potion);
			potionEffects.add(copy);
		}
		
		markDirty();
	}
	
	public static Kit createNewKit(PlayerEntity entityplayer, String name)
	{
		Kit kit = new Kit();
		kit.kitName = name;
		kit.createFrom(entityplayer);
		return kit;
	}
	
	public static void clearPlayerInvAndKit(PlayerEntity entityplayer)
	{
		entityplayer.inventory.clearMatchingItems($ -> true, -1);
		entityplayer.clearActivePotions();
	}
	
	public void markDirty()
	{
		needsSave = true;
	}
	
	public void markSaved()
	{
		needsSave = false;
	}
	
	public boolean needsSave()
	{
		return needsSave;
	}
	
	public boolean isDeleted()
	{
		return deleted;
	}
	
	public void deleteKit()
	{
		deleted = true;
		markDirty();
	}
	
	public void writeToNBT(CompoundNBT nbt)
	{
		nbt.putString("KitID", kitID.toString());
		nbt.putString("Name", kitName);
		nbt.putBoolean("Deleted", deleted);
		
		ListNBT armorTags = new ListNBT();
		for (int i = 0; i < armorItems.length; i++)
		{
			ItemStack armorItem = armorItems[i];
			if (!armorItem.isEmpty())
			{
				CompoundNBT itemData = new CompoundNBT();
				itemData.putByte("ArmorSlot", (byte)i);
				armorItem.write(itemData);
				armorTags.add(itemData);
			}
		}
		nbt.put("ArmorItems", armorTags);
		
		if (!heldItem.isEmpty())
		{
			CompoundNBT heldData = new CompoundNBT();
			heldItem.write(heldData);
			nbt.put("HeldItem", heldData);
		}
		
		if (!offhandItem.isEmpty())
		{
			CompoundNBT offhandData = new CompoundNBT();
			offhandItem.write(offhandData);
			nbt.put("OffhaneItem", offhandData);
		}
		
		if (!otherItems.isEmpty())
		{
			ListNBT otherTags = new ListNBT();
			for (ItemStack itemstack : otherItems)
			{
				CompoundNBT itemData = new CompoundNBT();
				itemstack.write(itemData);
				otherTags.add(itemData);
			}
			nbt.put("OtherItems", otherTags);
		}
		
		if (!potionEffects.isEmpty())
		{
			ListNBT potionTags = new ListNBT();
			for (EffectInstance potion : potionEffects)
			{
				CompoundNBT potionData = new CompoundNBT();
				potion.write(potionData);
				potionTags.add(potionData);
			}
			nbt.put("Potions", potionTags);
		}
	}
	
	public void readFromNBT(CompoundNBT nbt)
	{
		kitID = UUID.fromString(nbt.getString("KitID"));
		kitName = nbt.getString("Name");
		deleted = nbt.getBoolean("Deleted");
		
		Arrays.fill(armorItems, ItemStack.EMPTY);
		if (nbt.contains("ArmorItems", Constants.NBT.TAG_LIST))
		{
			ListNBT otherTags = nbt.getList("ArmorItems", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < otherTags.size(); i++)
			{
				CompoundNBT itemData = otherTags.getCompound(i);
				int slot = itemData.getByte("ArmorSlot");
				if (slot >= 0 && slot < armorItems.length)
				{
					ItemStack itemstack = ItemStack.read(itemData);
					if (!itemstack.isEmpty())
					{
						armorItems[slot] = itemstack;
					}
				}
			}
		}
		
		if (nbt.contains("HeldItem", Constants.NBT.TAG_COMPOUND))
		{
			CompoundNBT heldData = nbt.getCompound("HeldItem");
			heldItem = ItemStack.read(heldData);
		}
		
		if (nbt.contains("OffhandItem", Constants.NBT.TAG_COMPOUND))
		{
			CompoundNBT offhandData = nbt.getCompound("OffhandItem");
			offhandItem = ItemStack.read(offhandData);
		}
		
		otherItems.clear();
		if (nbt.contains("OtherItems", Constants.NBT.TAG_LIST))
		{
			ListNBT otherTags = nbt.getList("OtherItems", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < otherTags.size(); i++)
			{
				CompoundNBT itemData = otherTags.getCompound(i);
				ItemStack itemstack = ItemStack.read(itemData);
				if (!itemstack.isEmpty())
				{
					otherItems.add(itemstack);
				}
			}
		}
		
		potionEffects.clear();
		if (nbt.contains("Potions", Constants.NBT.TAG_LIST))
		{
			ListNBT potionTags = nbt.getList("Potions", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < potionTags.size(); i++)
			{
				CompoundNBT potionData = potionTags.getCompound(i);
				EffectInstance potion = EffectInstance.read(potionData);
				if (potion != null)
				{
					potionEffects.add(potion);
				}
			}
		}
	}

	//Addon start
	public boolean isSelfSupplied() {
		return false;
	}
	public static final UUID SSUUID = UUID.fromString("ad1d0c38-bdd4-4ad7-a6fe-0076df934bf8");
	public static final Kit SELF_SUPPLIED = new Kit() {
		@Override
		public boolean isSelfSupplied() {
			return true;
		}
	};
	static {
		setupSelfSupplied();
	}
	public static void setupSelfSupplied() {
		SELF_SUPPLIED.kitID = SSUUID;
		SELF_SUPPLIED.rename("_self_supplied");
		KitDatabase.kitMap.put(SELF_SUPPLIED.kitID, SELF_SUPPLIED);
		KitDatabase.kitNameMap.put(SELF_SUPPLIED.kitName, SELF_SUPPLIED.kitID);
	}
	//Addon end

}
