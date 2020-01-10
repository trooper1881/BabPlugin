package com.trooper.babplugin;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.Map;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/* *
 * Created by Joshua Bell (RingOfStorms)
 * 
 * Post explaining here: http://bukkit.org/threads/gsonfactory-gson-that-works-on-itemstack-potioneffect-location-objects.331161/
 * */
public class GsonFactory {

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD})
	public static @interface Ignore {}

	/*
	- I want to not use Bukkit parsing for most objects... it's kind of clunky
	- Instead... I want to start using any of Mojang's tags
	- They're really well documented + built into MC, and handled by them.
	- Rather than kill your old code, I'm going to write TypeAdapaters using Mojang's stuff.
	 */

	private static Gson g = new Gson();

	private static Gson prettyGson;
	private static Gson compactGson;

	/**
	 * Returns a Gson instance for use anywhere with new line pretty printing
	 * <p>
	 *    Use @GsonIgnore in order to skip serialization and deserialization
	 * </p>
	 * @return a Gson instance
	 */
	public static Gson getPrettyGson () {
		if (prettyGson == null)
			prettyGson = new GsonBuilder().addSerializationExclusionStrategy(new ExposeExlusion())
					.addDeserializationExclusionStrategy(new ExposeExlusion())
					.registerTypeAdapter(PotionEffect.class, new PotionEffectGsonAdapter())
					.setPrettyPrinting()
					.disableHtmlEscaping()
					.create();
		return prettyGson;
	}

	/**
	 * Returns a Gson instance for use anywhere with one line strings
	 * <p>
	 *    Use @GsonIgnore in order to skip serialization and deserialization
	 * </p>
	 * @return a Gson instance
	 */
	public static Gson getCompactGson () {
		if(compactGson == null)
			compactGson = new GsonBuilder().addSerializationExclusionStrategy(new ExposeExlusion())
					.addDeserializationExclusionStrategy(new ExposeExlusion())
					.registerTypeAdapter(PotionEffect.class, new PotionEffectGsonAdapter())
					.disableHtmlEscaping()
					.create();
		return compactGson;
	}

	/**
	 * Creates a new instance of Gson for use anywhere
	 * <p>
	 *    Use @GsonIgnore in order to skip serialization and deserialization
	 * </p>
	 * @return a Gson instance
	 */

	private static class ExposeExlusion implements ExclusionStrategy {
		@Override
		public boolean shouldSkipField(FieldAttributes fieldAttributes) {
			final Ignore ignore = fieldAttributes.getAnnotation(Ignore.class);
			if(ignore != null)
				return true;
			final Expose expose = fieldAttributes.getAnnotation(Expose.class);
			return expose != null && (!expose.serialize() || !expose.deserialize());
		}

		@Override
		public boolean shouldSkipClass(Class<?> aClass) {
			return false;
		}
	}


	private static class PotionEffectGsonAdapter extends TypeAdapter<PotionEffect> {

		private static Type seriType = new TypeToken<Map<String, Object>>(){}.getType();

		private static String TYPE = "effect";
		private static String DURATION = "duration";
		private static String AMPLIFIER = "amplifier";
		private static String AMBIENT = "ambient";

		@Override
		public void write(JsonWriter jsonWriter, PotionEffect potionEffect) throws IOException {
			if(potionEffect == null) {
				jsonWriter.nullValue();
				return;
			}
			jsonWriter.value(getRaw(potionEffect));
		}

		@Override
		public PotionEffect read(JsonReader jsonReader) throws IOException {
			if(jsonReader.peek() == JsonToken.NULL) {
				jsonReader.nextNull();
				return null;
			}
			return fromRaw(jsonReader.nextString());
		}

		private String getRaw (PotionEffect potion) {
			Map<String, Object> serial = potion.serialize();

			return g.toJson(serial);
		}

		private PotionEffect fromRaw (String raw) {
			Map<String, Object> keys = g.fromJson(raw, seriType);
			//return new PotionEffect(PotionEffectType.getById(((Double) keys.get(TYPE)).intValue()), ((Double) keys.get(DURATION)).intValue(), ((Double) keys.get(AMPLIFIER)).intValue(),  (Boolean) keys.get(AMBIENT));
			return new PotionEffect(PotionEffectType.getById(((Double) keys.get(TYPE)).intValue()), ((Double) keys.get(DURATION)).intValue(), ((Double) keys.get(AMPLIFIER)).intValue(),  (Boolean) keys.get(AMBIENT));
		}
	}

	

	
	
}