package com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.condition;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.advancements.predicates.CollectionPredicate;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.advancements.predicates.SingleComponentItemPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.properties.conditional.Broken;
import net.minecraft.client.renderer.item.properties.conditional.ComponentMatches;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.client.renderer.item.properties.conditional.CustomModelDataProperty;
import net.minecraft.client.renderer.item.properties.conditional.Damaged;
import net.minecraft.client.renderer.item.properties.conditional.HasComponent;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.AttributeModifiersPredicate;
import net.minecraft.core.component.predicates.BundlePredicate;
import net.minecraft.core.component.predicates.ContainerPredicate;
import net.minecraft.core.component.predicates.CustomDataPredicate;
import net.minecraft.core.component.predicates.DamagePredicate;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.core.component.predicates.FireworkExplosionPredicate;
import net.minecraft.core.component.predicates.FireworksPredicate;
import net.minecraft.core.component.predicates.JukeboxPlayablePredicate;
import net.minecraft.core.component.predicates.PotionsPredicate;
import net.minecraft.core.component.predicates.TrimPredicate;
import net.minecraft.core.component.predicates.VillagerTypePredicate;
import net.minecraft.core.component.predicates.WritableBookPredicate;
import net.minecraft.core.component.predicates.WrittenBookPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.hiword9.rprenames.util.Util.setAndFillMissing;

public non-sealed class BooleanCondition<P extends ConditionalItemModelProperty> extends AbstractPropertyValueCondition<P, Boolean> {
    private BooleanCondition(P property, boolean value) {
        super(property, value);
    }

    public static <P extends ConditionalItemModelProperty> BooleanCondition<P> of(
            P property, boolean value
    ) {
        return isPropertyApplicable(property)
                ? new ApplicableBooleanCondition<>(property, value)
                : new BooleanCondition<>(property, value);
    }

    private static boolean isPropertyApplicable(ConditionalItemModelProperty property) {
        for (var clazz : ApplicableBooleanCondition.APPLICABLE_PROPERTIES) {
            if (clazz.isInstance(property)) return true;
        }
        return false;
    }

    public static class ApplicableBooleanCondition<P extends ConditionalItemModelProperty>
            extends BooleanCondition<P>
            implements Applicable
    {
        private static final List<Class<?>> APPLICABLE_PROPERTIES = List.of(
                Broken.class,
                ComponentMatches.class,
                CustomModelDataProperty.class,
                Damaged.class,
                HasComponent.class
        );

        private ApplicableBooleanCondition(P property, boolean value) {
            super(property, value);
        }

        @Override
        public void apply(ItemStack stack) {
            switch (property) {
                case Broken prop -> applyBroken(stack, value);
                case ComponentMatches prop -> applyComponentBoolean(stack, prop, value);
                case CustomModelDataProperty prop -> applyCustomModelData(stack, prop, value);
                case Damaged prop -> applyDamaged(stack, value);
                case HasComponent prop -> applyHasComponent(stack, prop, value);
                case null, default -> {}
            }
        }

        private static void applyBroken(ItemStack stack, boolean value) {
            if (!stack.isDamageableItem()) return;
            stack.setDamageValue(value ? stack.getMaxDamage() - 1 : 0);
        }

        private static void applyComponentBoolean(ItemStack stack, ComponentMatches property, boolean value) {
            applyDataComponentPredicate(stack, property.predicate().predicate(), value);
        }

        private static void applyDataComponentPredicate(ItemStack stack, DataComponentPredicate predicate, boolean value) {
            switch (predicate) {
                case CustomDataPredicate p -> applyCustomDataPredicate(stack, p, value);
                case DamagePredicate p -> applyDamagePredicate(stack, p, value);
                case SingleComponentItemPredicate<?> p -> applySingleComponentPredicate(stack, p, value);
                case null, default -> {}
            }
        }

        private static void applyCustomDataPredicate(ItemStack stack, CustomDataPredicate predicate, boolean value) {
            var exists = stack.get(DataComponents.CUSTOM_DATA);
            if (value && predicate.value().tag() != null) {
                if (exists == null) {
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                    exists = stack.get(DataComponents.CUSTOM_DATA);
                    assert exists != null;
                }
                stack.set(
                        DataComponents.CUSTOM_DATA,
                        CustomData.of(exists.copyTag().merge(predicate.value().tag()))
                );
            } else if (predicate.matches(stack)) {
                // todo need to brake equality here somehow
            }
        }

        private static void applyDamagePredicate(ItemStack stack, DamagePredicate predicate, boolean value) {
            var maxDamageComponent = stack.getMaxDamage();

            var damageRangeFirst = predicate.damage();
            var damageRangeSecond = MinMaxBounds.Ints.between(
                    maxDamageComponent - predicate.durability().bounds().max().orElse(maxDamageComponent),
                    maxDamageComponent - predicate.durability().bounds().min().orElse(0)
            );

            var passRange = MinMaxBounds.Ints.between(
                    Math.max(
                            damageRangeFirst.bounds().min().orElse(0),
                            damageRangeSecond.bounds().min().orElse(0)
                    ),
                    Math.min(
                            damageRangeFirst.bounds().max().orElse(maxDamageComponent),
                            damageRangeSecond.bounds().max().orElse(maxDamageComponent)
                    )
            );

            if (value) {
                stack.setDamageValue(passRange.bounds().min().orElse(0));
            } else if (predicate.matches(stack)) {
                stack.setDamageValue(passRange.bounds().min().orElse(0) != 0
                        ? 0
                        : passRange.bounds().max().orElse(maxDamageComponent) + 1
                );
            }
        }

        private static void applyCustomModelData(ItemStack stack, CustomModelDataProperty property, boolean value) {
            var exists = stack.get(DataComponents.CUSTOM_MODEL_DATA);
            if (exists == null) {
                stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(
                        new ArrayList<>(),
                        setAndFillMissing(new ArrayList<>(), property.index(), value, false),
                        new ArrayList<>(),
                        new ArrayList<>()
                ));
            } else {
                setAndFillMissing(exists.flags(), property.index(), value, false);
            }
        }

        private static void applyDamaged(ItemStack stack, boolean value) {
            if (!stack.isDamageableItem()) return;
            stack.setDamageValue(value ? 1 : 0);
        }

        private static void applyHasComponent(ItemStack stack, HasComponent property, boolean value) {
            // todo account for ignoreDefault
            if (stack.has(property.componentType()) == value) return;

            if (value) {
                // todo set empty component
            } else {
                stack.remove(property.componentType());
            }
        }

        private static void applySingleComponentPredicate(
                ItemStack stack, SingleComponentItemPredicate<?> predicate, boolean value
        ) {
            if (!value) {
                stack.remove(predicate.componentType());
                return;
            }
            switch (predicate) {
                case AttributeModifiersPredicate p -> applyAttributeModifiers(stack, p);
                case BundlePredicate p -> applyBundle(stack, p);
                case ContainerPredicate p -> applyContainer(stack, p);
                case EnchantmentsPredicate p -> applyEnchantments(stack, p);
                case FireworkExplosionPredicate p -> applyFireworkExplosion(stack, p);
                case FireworksPredicate p -> applyFireworks(stack, p);
                case JukeboxPlayablePredicate p -> applyJukeboxPlayable(stack, p);
                case PotionsPredicate p -> applyPotions(stack, p);
                case TrimPredicate p -> applyTrim(stack, p);
                case VillagerTypePredicate p -> applyVillagerType(stack, p);
                case WritableBookPredicate p -> applyWritableBook(stack, p);
                case WrittenBookPredicate p -> applyWrittenBook(stack, p);
                default -> {}
            }
        }

        private static void applyPotions(ItemStack stack, PotionsPredicate predicate) {
            var potion = firstHolder(predicate.potions(), Registries.POTION);
            if (potion == null) return;
            stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
        }

        private static void applyVillagerType(ItemStack stack, VillagerTypePredicate predicate) {
            var type = firstHolder(predicate.villagerTypes(), Registries.VILLAGER_TYPE);
            if (type == null) return;
            stack.set(DataComponents.VILLAGER_VARIANT, type);
        }

        private static void applyJukeboxPlayable(ItemStack stack, JukeboxPlayablePredicate predicate) {
            var song = predicate.song()
                    .map(set -> firstHolder(set, Registries.JUKEBOX_SONG))
                    .orElseGet(() -> anyHolder(Registries.JUKEBOX_SONG));
            if (song == null) return;
            stack.set(DataComponents.JUKEBOX_PLAYABLE, new JukeboxPlayable(song));
        }

        private static void applyTrim(ItemStack stack, TrimPredicate predicate) {
            var material = predicate.material()
                    .map(set -> firstHolder(set, Registries.TRIM_MATERIAL))
                    .orElseGet(() -> anyHolder(Registries.TRIM_MATERIAL));
            var pattern = predicate.pattern()
                    .map(set -> firstHolder(set, Registries.TRIM_PATTERN))
                    .orElseGet(() -> anyHolder(Registries.TRIM_PATTERN));
            if (material == null || pattern == null) return;
            stack.set(DataComponents.TRIM, new ArmorTrim(material, pattern));
        }

        private static void applyEnchantments(ItemStack stack, EnchantmentsPredicate predicate) {
            var enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            for (var wanted : predicate.enchantments()) {
                var enchantment = wanted.enchantments()
                        .map(set -> firstHolder(set, Registries.ENCHANTMENT))
                        .orElseGet(() -> anyHolder(Registries.ENCHANTMENT));
                if (enchantment == null) continue;
                enchantments.set(enchantment, Math.max(1, intInBounds(wanted.level())));
            }
            stack.set(predicate.componentType(), enchantments.toImmutable());
        }

        private static void applyFireworkExplosion(ItemStack stack, FireworkExplosionPredicate predicate) {
            stack.set(DataComponents.FIREWORK_EXPLOSION, fireworkExplosionFor(predicate.predicate()));
        }

        private static void applyFireworks(ItemStack stack, FireworksPredicate predicate) {
            var explosions = buildCollection(
                    predicate.explosions(),
                    ApplicableBooleanCondition::fireworkExplosionFor,
                    () -> FireworkExplosion.DEFAULT
            );
            stack.set(DataComponents.FIREWORKS, new Fireworks(intInBounds(predicate.flightDuration()), explosions));
        }

        private static FireworkExplosion fireworkExplosionFor(FireworkExplosionPredicate.FireworkPredicate predicate) {
            return new FireworkExplosion(
                    predicate.shape().orElse(FireworkExplosion.Shape.SMALL_BALL),
                    IntList.of(),
                    IntList.of(),
                    predicate.trail().orElse(false),
                    predicate.twinkle().orElse(false)
            );
        }

        private static void applyWritableBook(ItemStack stack, WritableBookPredicate predicate) {
            var pages = buildCollection(
                    predicate.pages(),
                    page -> Filterable.passThrough(page.contents()),
                    () -> Filterable.passThrough("")
            );
            stack.set(DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent(pages));
        }

        private static void applyWrittenBook(ItemStack stack, WrittenBookPredicate predicate) {
            var pages = buildCollection(
                    predicate.pages(),
                    page -> Filterable.passThrough(page.contents()),
                    () -> Filterable.passThrough((Component) Component.empty())
            );
            stack.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                    Filterable.passThrough(predicate.title().orElse("")),
                    predicate.author().orElse(""),
                    intInBounds(predicate.generation()),
                    pages,
                    predicate.resolved().orElse(false)
            ));
        }

        private static void applyAttributeModifiers(ItemStack stack, AttributeModifiersPredicate predicate) {
            var entries = buildCollection(
                    predicate.modifiers(),
                    ApplicableBooleanCondition::attributeEntryFor,
                    () -> attributeEntryFor(null)
            );
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, new ItemAttributeModifiers(entries));
        }

        private static ItemAttributeModifiers.Entry attributeEntryFor(AttributeModifiersPredicate.EntryPredicate predicate) {
            Holder<Attribute> attribute = predicate == null
                    ? null
                    : predicate.attribute().map(set -> firstHolder(set, Registries.ATTRIBUTE)).orElse(null);
            if (attribute == null) {
                attribute = anyHolder(Registries.ATTRIBUTE);
            }
            if (attribute == null) return null;
            var id = predicate != null && predicate.id().isPresent()
                    ? predicate.id().get()
                    : Identifier.withDefaultNamespace("preview");
            var amount = predicate == null ? 0.0 : doubleInBounds(predicate.amount());
            var operation = predicate == null
                    ? AttributeModifier.Operation.ADD_VALUE
                    : predicate.operation().orElse(AttributeModifier.Operation.ADD_VALUE);
            var slot = predicate == null ? EquipmentSlotGroup.ANY : predicate.slot().orElse(EquipmentSlotGroup.ANY);
            return new ItemAttributeModifiers.Entry(attribute, new AttributeModifier(id, amount, operation), slot);
        }

        private static void applyBundle(ItemStack stack, BundlePredicate predicate) {
            var items = buildCollection(predicate.items(), ApplicableBooleanCondition::itemFor, () -> new ItemStack(Items.STONE));
            stack.set(
                    DataComponents.BUNDLE_CONTENTS,
                    new BundleContents(items.stream().map(ItemStackTemplate::fromStack).toList())
            );
        }

        private static void applyContainer(ItemStack stack, ContainerPredicate predicate) {
            var items = buildCollection(predicate.items(), ApplicableBooleanCondition::itemFor, () -> new ItemStack(Items.STONE));
            stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
        }

        private static ItemStack itemFor(ItemPredicate predicate) {
            var item = predicate.items().map(set -> firstHolder(set, Registries.ITEM)).orElse(null);
            var itemStack = item == null ? new ItemStack(Items.STONE) : new ItemStack(item.value());
            itemStack.setCount(Math.max(1, intInBounds(predicate.count())));
            var matchers = predicate.components();
            if (!matchers.exact().isEmpty()) itemStack.applyComponents(matchers.exact().asPatch());
            matchers.partial().values().forEach(sub -> applyDataComponentPredicate(itemStack, sub, true));
            return itemStack;
        }

        private static <T, P extends Predicate<T>, E> List<E> buildCollection(
                Optional<CollectionPredicate<T, P>> collection,
                Function<P, E> fromPredicate,
                Supplier<E> filler
        ) {
            var result = new ArrayList<E>();
            collection.ifPresent(c -> {
                c.contains().ifPresent(contains ->
                        contains.unpack().forEach(p -> addIfNotNull(result, fromPredicate.apply(p))));
                c.counts().ifPresent(counts -> counts.unpack().forEach(entry -> {
                    for (int i = 0; i < Math.max(1, intInBounds(entry.count())); i++) {
                        addIfNotNull(result, fromPredicate.apply(entry.test()));
                    }
                }));
                c.size().ifPresent(size -> {
                    int target = intInBounds(size);
                    while (result.size() < target) {
                        var element = filler.get();
                        if (element == null) break;
                        result.add(element);
                    }
                });
            });
            return result;
        }

        private static <E> void addIfNotNull(List<E> list, E element) {
            if (element != null) list.add(element);
        }

        private static <T> Holder<T> firstHolder(HolderSet<T> set, ResourceKey<? extends Registry<? extends T>> registry) {
            return set.stream().findFirst()
                    .flatMap(Holder::unwrapKey)
                    .flatMap(key -> liveRegistry(registry).flatMap(r -> r.get(key)))
                    .orElse(null);
        }

        private static <T> Holder<T> anyHolder(ResourceKey<? extends Registry<? extends T>> registry) {
            return liveRegistry(registry).flatMap(Registry::getAny).orElse(null);
        }

        private static <T> Optional<Registry<T>> liveRegistry(ResourceKey<? extends Registry<? extends T>> registry) {
            var minecraft = Minecraft.getInstance();
            if (minecraft.level == null) return Optional.empty();
            return minecraft.level.registryAccess().lookup(registry);
        }

        private static int intInBounds(MinMaxBounds.Ints bounds) {
            return bounds.bounds().min().orElse(bounds.bounds().max().orElse(0));
        }

        private static double doubleInBounds(MinMaxBounds.Doubles bounds) {
            return bounds.bounds().min().orElse(bounds.bounds().max().orElse(0.0));
        }
    }
}
