package uk.ac.york.sepr4.object.building;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import lombok.Data;
import uk.ac.york.sepr4.object.entity.NPCBoat;
import uk.ac.york.sepr4.object.entity.NPCBoss;
import uk.ac.york.sepr4.object.entity.Player;
import uk.ac.york.sepr4.screen.GameScreen;

import java.util.Optional;

@Data
public class BuildingManager {

    private Array<College> colleges;
    private Array<Department> departments;

    private GameScreen gameScreen;

    //time till next spawn attempt
    private float spawnDelta;

    /***
     * This class handles instances of buildings (Colleges and Departments)
     *
     * It is responsible for loading from file and making sure the map object relating to this building is present.
     * There is a method which arranges spawning of college enemies.
     * @param gameScreen
     */
    public BuildingManager(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        this.spawnDelta = 0f;

        if(gameScreen.getPirateMap().isObjectsEnabled()) {
            this.colleges = new Array<>();
            this.departments = new Array<>();
            Json json = new Json();
            Gdx.app.debug("BuildingManager", "Loading Buildings");
            loadColleges(json.fromJson(Array.class, College.class, Gdx.files.internal("colleges.json")));
            loadDepartments(json.fromJson(Array.class, Department.class, Gdx.files.internal("departments.json")));
        } else {
            Gdx.app.log("Building Manager", "Objects not enabled, not loading buildings!");
        }
    }

    //TODO: Could have a cooldown here
    public void checkBossSpawn() {
        for(College college : colleges) {
            if(!college.isBossSpawned()) {
                Player player = gameScreen.getEntityManager().getPlayer();
                if (college.getCollegeZone().contains(player.getRectBounds())) {
                    Gdx.app.debug("BuildingManager", "Player entered college zone: " + college.getName());
                    NPCBoss npcBoss = college.spawnBoss(gameScreen.getEntityManager().getNextEnemyID());
                    college.setBossSpawned(true);
                    gameScreen.getEntityManager().addNPC(npcBoss);
                }
            }
        }
    }

    public void spawnCollegeEnemies(float delta) {
        spawnDelta+=delta;
        if(spawnDelta >= 1f) {
            for (College college : this.colleges) {
                if(gameScreen.getEntityManager().getLivingEntitiesInArea(college.getCollegeSpawnZone()).size
                        < college.getMaxEntities()) {
                    Optional<NPCBoat> optionalEnemy = college.generateCollegeNPC(gameScreen.getEntityManager().getNextEnemyID());
                    if (optionalEnemy.isPresent()) {
                        Gdx.app.debug("Building Manager", "Spawning an enemy at " + college.getName());
                        gameScreen.getEntityManager().addNPC(optionalEnemy.get());
                    }
                } else {
                    Gdx.app.debug("BuildingManager", "Max entities @ "+college.getName());
                }
            }
            spawnDelta = 0f;
        }
    }

    private void loadColleges(Array<College> loading) {
        for(College college : loading) {
            if (college.load(gameScreen.getPirateMap())) {
                    colleges.add(college);
                    Gdx.app.debug("BuildingManager", "Loaded " + college.getName());
                } else {
                    Gdx.app.error("BuildingManager", "Failed to load " + college.getName());
                }

        }
    }

    private void loadDepartments(Array<Department> loading) {
        for(Department department : loading) {
            if (department.load(gameScreen.getPirateMap())) {
                    departments.add(department);
                    Gdx.app.debug("BuildingManager", "Loaded " + department.getName());
                } else {
                    Gdx.app.error("BuildingManager", "Failed to load " + department.getName());

                }

        }
    }
}
