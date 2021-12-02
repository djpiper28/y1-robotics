package common;

/**
 * This is the parent class to emotions so that both the app and lejos have the
 * same class definition
 * 
 * @author Danny
 * @version 1.1
 */
public abstract class EmotionsInterface {

	private float boredom;
	/**
	 * Aggregate stats
	 */
	private float courage;
	private float curiosity;

	/**
	 * Primitive stats All the float attributes, range between 1 and 0 (0 being
	 * none, and 1 being most) So if 0 hunger -> Not hungry If 1 hunger -> Dead
	 * (starved)
	 * 
	 * @since 1.1
	 */
	private float fear;
	private float happiness;
	private float hunger;
	private float tired;

	/**
	 * Sets every value to 0.5f, apart from hunger which is determined on battery
	 * power. Calculates overall emotion.
	 * 
	 * @since 1.0
	 */
	public EmotionsInterface() {
		this.hunger = 0f;
		this.boredom = 0f;
		this.tired = 0f;
		this.fear = 0f;

		this.updateAll();
	}

	/**
	 * @return float, boredom. a value betwen -1 and 1 inclusive that represents how
	 *         bored the robot is
	 * @since 1.0
	 */
	public float getBoredom() {
		return this.boredom;
	}

	/**
	 * @return float emotion. a value betwen -1 and 1 inclusive that represents how
	 *         tired the robot is
	 * @since 1.0
	 */
	public float getCourage() {
		return this.courage;
	}

	/**
	 * @return float emotion. a value betwen -1 and 1 inclusive that represents how
	 *         tired the robot is
	 * @since 1.0
	 */
	public float getCuriosity() {
		return this.curiosity;
	}

	/**
	 * @return float fear. a value betwen -1 and 1 inclusive that represents how
	 *         scared the robot is
	 * @since 1.0
	 */
	public float getFear() {
		return this.fear;
	}

	/**
	 * @return float emotion. a value betwen -1 and 1 inclusive that represents how
	 *         tired the robot is
	 * @since 1.0
	 */
	public float getHappiness() {
		return this.happiness;
	}

	/**
	 * @return float hunger. a value betwen -1 and 1 inclusive that represents how
	 *         tired the robot is
	 * @since 1.0
	 */
	public float getHunger() {
		return this.hunger;
	}

	/**
	 * @return float tired. a value betwen -1 and 1 inclusive that represents how
	 *         tired the robot is
	 * @since 1.0
	 */
	public float getTired() {
		return this.tired;
	}

	/**
	 * Override to react to changes to state
	 * 
	 * @param float newValue is the new value
	 * @since 1.0
	 */
	public abstract void onBordemSet(float newBordem);

	/**
	 * Override to react to changes to state
	 * 
	 * @param float newValue is the new value
	 * @since 1.0
	 */
	public abstract void onCourageSet(float newEmotion);

	/**
	 * Override to react to changes to state
	 * 
	 * @param float newValue is the new value
	 * @since 1.0
	 */
	public abstract void onCuriositySet(float newEmotion);

	/**
	 * Override to react to changes to state
	 * 
	 * @param float newValue is the new value
	 * @since 1.1
	 */
	public abstract void onFearSet(float newFear);

	/**
	 * Override to react to changes to state
	 * 
	 * @param float newValue is the new value
	 * @since 1.0
	 */
	public abstract void onHappinessSet(float newEmotion);

	/**
	 * Override to react to changes to state
	 * 
	 * @param float newValue is the new value
	 * @since 1.0
	 */
	public abstract void onHungerSet(float newHunger);

	/**
	 * Override to react to changes to state
	 * 
	 * @param float newValue is the new value
	 * @since 1.0
	 */
	public abstract void onTiredSet(float newTired);

	/**
	 * @param boredem sets the bordem value (should be between -1 and 1)
	 * @since 1.0
	 */
	public void setBoredem(float boredem) {
		this.boredom = boredem;
		if (this.boredom < 0)
			this.boredom = 0;
		if (this.boredom > 1)
			this.boredom = 1;
		this.onBordemSet(this.boredom);
		updateAll();
	}

	/**
	 * Sets the new courage level
	 * 
	 * @since 1.0
	 */
	public void setCourage() {
		this.courage = (this.happiness + this.fear) / 2;
		onCourageSet(this.courage);
	}

	/**
	 * Set the curiosity level
	 * 
	 * @since 1.0
	 */
	public void setCuriosity() {
		this.curiosity = (this.boredom + (1 - this.tired) + this.courage) / 3;
		this.onCuriositySet(this.curiosity);
	}

	/**
	 * @param fear, sets the tired value (should be between -1 and 1)
	 * @since 1.0
	 */
	public void setFear(float newFear) {
		this.fear = newFear;
		if (this.fear < 0)
			this.fear = 0;
		if (this.fear > 1)
			this.fear = 1;
		this.onFearSet(this.fear);
		updateAll();
	}

	/**
	 * Every time an emotion changes, the overall emotion is changed, This is a
	 * simple average of all 3. Private because it shouldn't be called externally.
	 * 
	 * @since 1.0
	 */
	public void setHappiness() {
		this.happiness = (this.hunger + this.boredom + this.tired) / 3;
		onHappinessSet(this.happiness);
	}

	/**
	 * @param hunger, sets the hunger value (should be between -1 and 1)
	 * @since 1.0
	 */
	public void setHunger(float hunger) {
		this.hunger = hunger;
		if (this.hunger < 0)
			this.hunger = 0;
		if (this.hunger > 1)
			this.hunger = 1;
		this.onHungerSet(this.hunger);
		updateAll();
	}

	/**
	 * @param tired, sets the tired value (should be between -1 and 1)
	 * @since 1.0
	 */
	public void setTired(float tired) {
		this.tired = tired;
		if (this.tired < 0)
			this.tired = 0;
		if (this.tired > 1)
			this.tired = 1;
		this.onTiredSet(this.tired);
		updateAll();
	}

	/**
	 * This will update the non-primitive stats
	 * 
	 * @since 1.1
	 */
	private void updateAll() {
		this.setHappiness();
		this.setCourage();
		this.setCuriosity();
	}

}
