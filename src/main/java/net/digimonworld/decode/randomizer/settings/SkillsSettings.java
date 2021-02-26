package net.digimonworld.decode.randomizer.settings;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;

import org.controlsfx.control.ToggleSwitch;

import de.phoenixstaffel.decodetools.keepdata.Finisher;
import de.phoenixstaffel.decodetools.keepdata.GlobalKeepData;
import de.phoenixstaffel.decodetools.keepdata.LanguageKeep;
import de.phoenixstaffel.decodetools.keepdata.Skill;
import de.phoenixstaffel.decodetools.keepdata.enums.MoveKind;
import de.phoenixstaffel.decodetools.keepdata.enums.Status;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import net.digimonworld.decode.randomizer.RandoLogger.LogLevel;
import net.digimonworld.decode.randomizer.RandomizationContext;

public class SkillsSettings implements Setting {
    /*-
     * MP Cost
     *   enable
     * Cooldown
     *   enable
     * Learn Rate
     *   enable
     * Damage
     *   enable
     * Status
     *   enable
     * Status chance
     *   enable
     */
    // TODO Tooltips
    
    private static final String FORMAT_FLOAT = "%28s | %12s | %#.4f -> %#.4f";
    private static final String FORMAT_INT = "%28s | %12s | %4d -> %4d";
    private static final String FORMAT_STRING = "%28s | %12s | %8s -> %8s";
    
    private BooleanProperty randomizeMPCost = new SimpleBooleanProperty();
    private BooleanProperty randomizeCooldown = new SimpleBooleanProperty();
    private BooleanProperty randomizeLearnRate = new SimpleBooleanProperty();
    private BooleanProperty randomizeDamage = new SimpleBooleanProperty();
    private BooleanProperty randomizeStatus = new SimpleBooleanProperty();
    private BooleanProperty randomizeStatusChance = new SimpleBooleanProperty();
    private BooleanProperty randomizeFinisher = new SimpleBooleanProperty();
    
    @Override
    public TitledPane create(GlobalKeepData data, LanguageKeep language) {
        VBox vbox = new VBox(4);
        TitledPane pane = new TitledPane("Skills", vbox);
        vbox.setAlignment(Pos.TOP_RIGHT);
        pane.setCollapsible(false);
        
        ToggleSwitch mpSwitch = new ToggleSwitch("MP Cost");
        mpSwitch.selectedProperty().bindBidirectional(randomizeMPCost);
        ToggleSwitch cooldownSwitch = new ToggleSwitch("Cooldown");
        cooldownSwitch.selectedProperty().bindBidirectional(randomizeCooldown);
        ToggleSwitch learnSwitch = new ToggleSwitch("Learn Rate");
        learnSwitch.selectedProperty().bindBidirectional(randomizeLearnRate);
        ToggleSwitch powerSwitch = new ToggleSwitch("Power");
        powerSwitch.selectedProperty().bindBidirectional(randomizeDamage);
        ToggleSwitch statusSwitch = new ToggleSwitch("Status");
        statusSwitch.selectedProperty().bindBidirectional(randomizeStatus);
        ToggleSwitch statusChanceSwitch = new ToggleSwitch("Status Chance");
        statusChanceSwitch.selectedProperty().bindBidirectional(randomizeStatusChance);
        ToggleSwitch finisherSwitch = new ToggleSwitch("Finisher");
        finisherSwitch.selectedProperty().bindBidirectional(randomizeFinisher);
        finisherSwitch.setTooltip(new Tooltip("Power and Status effect, is enabled."));
        
        vbox.getChildren().addAll(mpSwitch, cooldownSwitch, learnSwitch, powerSwitch, statusSwitch, statusChanceSwitch, finisherSwitch);
        return pane;
    }
    
    public void setRandomizeMPCost(boolean set) {
        randomizeMPCost.set(set);
    }
    
    @Override
    public void randomize(RandomizationContext context) {
        if (randomizeMPCost.get())
            randomizeMPCost(context);
        if (randomizeCooldown.get())
            randomizeCooldown(context);
        if (randomizeLearnRate.get())
            randomizeLearnRate(context);
        if (randomizeDamage.get())
            randomizeDamage(context);
        if (randomizeStatus.get())
            randomizeStatus(context);
        if (randomizeStatusChance.get())
            randomizeStatusChance(context);
        if (randomizeFinisher.get())
            randomizeFinisher(context);
    }
    
    private void randomizeLearnRate(RandomizationContext context) {
        Random rand = new Random(context.getInitialSeed() * "LearnChance".hashCode());
        
        context.logLine(LogLevel.ALWAYS, "Randomizing Learn Rates...");
        
        for (ListIterator<Skill> itr = context.getGlobalKeepData().getSkills().listIterator(); itr.hasNext();) {
            int i = itr.nextIndex();
            Skill a = itr.next();
            
            if (a.getTier() == 0)
                continue;
            
            float newLearnRate = 0.1f + 0.9f * rand.nextFloat();
            context.logLine(LogLevel.CASUAL,
                            String.format(FORMAT_FLOAT,
                                          context.getLanguageKeep().getSkillNames().getStringById(i + 1),
                                          "Learn Rate",
                                          a.getLearning(),
                                          newLearnRate));
            a.setLearning(newLearnRate);
        }
        
        context.logLine(LogLevel.ALWAYS, "");
    }
    
    private void randomizeFinisher(RandomizationContext context) {
        Random randDamage = new Random(context.getInitialSeed() * "FinisherDamage".hashCode());
        Random randStatus = new Random(context.getInitialSeed() * "FinisherStatus".hashCode());
        
        context.logLine(LogLevel.ALWAYS, "Randomizing Finisher...");
        
        for (ListIterator<Finisher> itr = context.getGlobalKeepData().getFinisher().listIterator(); itr.hasNext();) {
            int i = itr.nextIndex();
            Finisher a = itr.next();
            
            if (randomizeDamage.get()) {
                int power = 250 + randDamage.nextInt(750);
                context.logLine(LogLevel.CASUAL,
                                String.format(FORMAT_INT, context.getLanguageKeep().getFinisherNames().getStringById(i + 1), "Power", a.getPower(), power));
                a.setPower(power);
            }
            
            if (randomizeStatus.get()) {
                Status status = randStatus.nextDouble() > 0.75 ? Status.NONE : Status.values()[randStatus.nextInt(6) + 1];
                context.logLine(LogLevel.CASUAL,
                                String.format(FORMAT_STRING,
                                              context.getLanguageKeep().getFinisherNames().getStringById(i + 1),
                                              "Status",
                                              a.getEffect(),
                                              status));
                a.setEffect(status);
                a.setChance((short) (status == Status.NONE ? 0 : 100));
            }
        }
        
        context.logLine(LogLevel.ALWAYS, "");
    }
    
    private void randomizeCooldown(RandomizationContext context) {
        Random rand = new Random(context.getInitialSeed() * "Cooldown".hashCode());
        
        context.logLine(LogLevel.ALWAYS, "Randomizing Cooldowns...");
        
        for (ListIterator<Skill> itr = context.getGlobalKeepData().getSkills().listIterator(); itr.hasNext();) {
            int i = itr.nextIndex();
            Skill a = itr.next();
            
            if (a.getKind() != MoveKind.ATTACK || a.getTier() == 0)
                continue;
            
            short newCooldown = (short) (500 + rand.nextInt(6500));
            context.logLine(LogLevel.CASUAL,
                            String.format(FORMAT_INT,
                                          context.getLanguageKeep().getSkillNames().getStringById(i + 1),
                                          "Cooldown",
                                          a.getCooldown(),
                                          newCooldown));
            a.setCooldown(newCooldown);
        }
        
        context.logLine(LogLevel.ALWAYS, "");
    }
    
    private void randomizeDamage(RandomizationContext context) {
        Random rand = new Random(context.getInitialSeed() * "Power".hashCode());
        
        context.logLine(LogLevel.ALWAYS, "Randomizing Power...");
        
        for (ListIterator<Skill> itr = context.getGlobalKeepData().getSkills().listIterator(); itr.hasNext();) {
            int i = itr.nextIndex();
            Skill a = itr.next();
            
            if (a.getKind() != MoveKind.ATTACK)
                continue;
            
            int newDamage = 50 + rand.nextInt(200) + (rand.nextInt(1024) * rand.nextInt(1 + rand.nextInt(749))) / 1024;
            context.logLine(LogLevel.CASUAL,
                            String.format(FORMAT_INT, context.getLanguageKeep().getSkillNames().getStringById(i + 1), "Power", a.getDamage(), newDamage));
            a.setDamage(newDamage);
        }
        
        context.logLine(LogLevel.ALWAYS, "");
    }
    
    private void randomizeStatus(RandomizationContext context) {
        Random rand = new Random(context.getInitialSeed() * "Status".hashCode());
        
        context.logLine(LogLevel.ALWAYS, "Randomizing Status Effects...");
        
        for (ListIterator<Skill> itr = context.getGlobalKeepData().getSkills().listIterator(); itr.hasNext();) {
            int i = itr.nextIndex();
            Skill a = itr.next();
            
            if (a.getKind() != MoveKind.ATTACK || a.getTier() == 0)
                continue;
            
            Status newStatus = rand.nextBoolean() ? Status.NONE : Status.values()[rand.nextInt(6) + 1];
            context.logLine(LogLevel.CASUAL,
                            String.format(FORMAT_STRING, context.getLanguageKeep().getSkillNames().getStringById(i + 1), "Status", a.getStatus(), newStatus));
            a.setStatus(newStatus);
        }
        
        context.logLine(LogLevel.ALWAYS, "");
    }
    
    private void randomizeStatusChance(RandomizationContext context) {
        Random rand = new Random(context.getInitialSeed() * "StatusChance".hashCode());
        
        context.logLine(LogLevel.ALWAYS, "Randomizing Status Chances...");
        
        for (ListIterator<Skill> itr = context.getGlobalKeepData().getSkills().listIterator(); itr.hasNext();) {
            int i = itr.nextIndex();
            Skill a = itr.next();
            
            if (a.getStatus() == Status.NONE || a.getKind() != MoveKind.ATTACK || a.getTier() == 0)
                continue;
            
            short newStatusChance = (short) (1 + rand.nextInt(99));
            
            context.logLine(LogLevel.CASUAL,
                            String.format(FORMAT_INT,
                                          context.getLanguageKeep().getSkillNames().getStringById(i + 1),
                                          "StatusChance",
                                          a.getStatusChance(),
                                          newStatusChance));
            a.setStatusChance(newStatusChance);
        }
        
        context.logLine(LogLevel.ALWAYS, "");
    }
    
    private void randomizeMPCost(RandomizationContext context) {
        Random rand = new Random(context.getInitialSeed() * "MPCost".hashCode());
        
        context.logLine(LogLevel.ALWAYS, "Randomizing MP Cost...");
        
        final int mean = 250;
        final int stdDev = 150;
        
        for (ListIterator<Skill> itr = context.getGlobalKeepData().getSkills().listIterator(); itr.hasNext();) {
            int i = itr.nextIndex();
            Skill a = itr.next();
            
            int val = (int) (mean + (rand.nextGaussian() * stdDev));
            if (val < 5 || val > 995 || rand.nextDouble() < (500 - val) / 3000D)
                val = 5 + rand.nextInt(994);
            
            context.logLine(LogLevel.CASUAL,
                            String.format(FORMAT_INT, context.getLanguageKeep().getSkillNames().getStringById(i + 1), "MP Cost", a.getMpCost(), val));
            a.setMpCost((short) val);
        }
        
        context.logLine(LogLevel.ALWAYS, "");
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        
        map.put("randomizeMPCosts", randomizeMPCost.get());
        map.put("randomizeCooldown", randomizeCooldown.get());
        map.put("randomizeLearnRate", randomizeLearnRate.get());
        map.put("randomizeDamage", randomizeDamage.get());
        map.put("randomizeStatus", randomizeStatus.get());
        map.put("randomizeStatusChance", randomizeStatusChance.get());
        map.put("randomizeFinisher", randomizeFinisher.get());
        
        return map;
    }
    
    @Override
    public void load(Map<String, Object> map) {
        this.randomizeMPCost.set(Boolean.parseBoolean(map.getOrDefault("randomizeMPCosts", false).toString()));
        this.randomizeCooldown.set(Boolean.parseBoolean(map.getOrDefault("randomizeCooldown", false).toString()));
        this.randomizeLearnRate.set(Boolean.parseBoolean(map.getOrDefault("randomizeLearnRate", false).toString()));
        this.randomizeDamage.set(Boolean.parseBoolean(map.getOrDefault("randomizeDamage", false).toString()));
        this.randomizeStatus.set(Boolean.parseBoolean(map.getOrDefault("randomizeStatus", false).toString()));
        this.randomizeStatusChance.set(Boolean.parseBoolean(map.getOrDefault("randomizeStatusChance", false).toString()));
        this.randomizeFinisher.set(Boolean.parseBoolean(map.getOrDefault("randomizeFinisher", false).toString()));
    }
}
