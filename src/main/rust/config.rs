use std::collections::HashMap;
use std::fmt::{Display, Formatter};
use std::fs::File;
use std::hash::{Hash, Hasher};
use std::io::{BufRead, BufReader, BufWriter, Write};
use std::path::Path;

use crate::errors::{Error, PrintErr};

struct Config {
    values: HashMap<ConfigKey, String>,
}

pub struct ConfigKey {
    key: &'static str,
}


impl PartialEq<Self> for ConfigKey {
    fn eq(&self, other: &Self) -> bool {
        self.key == other.key
    }
}

impl Eq for ConfigKey {}

impl Hash for ConfigKey {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.key.hash(state)
    }
}

pub const GIT_URL: ConfigKey = ConfigKey { key: "git_url" };
pub const HOSTNAME: ConfigKey = ConfigKey { key: "hostname" };

impl Config {
    fn new() -> Config {
        let mut values = HashMap::new();

        values.insert(GIT_URL, "git_url".to_string());
        values.insert(HOSTNAME, "hostname".to_string());

        return Config {
            values
        };
    }
    pub fn set(&mut self, key: ConfigKey, value: String) {
        self.values.insert(key, value);
    }
    pub fn get(&self, key: ConfigKey) -> Option<&String> {
        self.values.get(&key)
    }

    pub fn load_file(path: &Path) -> Result<Config, Error> {
        let file = File::open(path);
        if let Err(err) = file {
            return PrintErr::from_message_error("cold not open config file", Box::from(err));
        }

        let lines = BufReader::new(file.unwrap()).lines();
        let mut config = Config::new();
        for line in lines {
            if let Err(e) = line {
                return PrintErr::from_message_error("could not read config", Box::from(e));
            }

            let line = line.unwrap();

            if line.starts_with("#") { continue; }

            let mut values = line.split(": ");
            let key = values.next();
            let value = values.next();
            if key == None || value == None || values.next() != None {
                return PrintErr::from_message("cold not parse config");
            }

            config.set(ConfigKey { key: key.unwrap() }, value.unwrap().to_string());
        }

        return Ok(config);
    }

    pub fn save_file(path: &Path, config: Config) -> Result<(), Error> {
        let file = File::open(path);
        if let Err(err) = file {
            return PrintErr::from_message_error("cold not open config file", Box::from(err));
        }

        let mut writer = BufWriter::new(file.unwrap());
        for value in config.values.values() {
            let result = writer.write_all((value.clone() + "\n").as_bytes());
            if let Err(e) = result {
                return PrintErr::from_message_error("cold not write config", Box::from(e));
            }
        }

        return Ok(());
    }
}