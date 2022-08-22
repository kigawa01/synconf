use std::env::current_exe;
use std::fs::copy;
use std::path::PathBuf;

use crate::errors::{Error, PrintErr};

pub fn command_install() {
    println!("install synconf...");

    match copy_os_binary() {
        Ok(_) => {}
        Err(_) => {
            return;
        }
    }

    println!("installed synconf");
}


#[cfg(target_os = "linux")]
fn copy_os_binary() -> Result<(), Error> {
    return copy_binary("/usr/local/bin/synconf");
}

#[cfg(not(target_os = "linux"))]
fn copy_os_binary() -> Result<(), Error> {
    let _ = copy_binary("");
    return PrintErr::from_message("non-linux is not supported");
}

fn copy_binary(to_path_str: &str) -> Result<(), Error> {
    println!("coping binary file...");
    let from_path: PathBuf;
    match current_exe() {
        Ok(path) => { from_path = path; }
        Err(err) => {
            return PrintErr::from_message_error("could not get current binary file path", Box::from(err));
        }
    }

    let to_path = PathBuf::from(to_path_str);

    match copy(from_path.as_path(), to_path.as_path()) {
        Ok(_) => {}
        Err(err) => {
            return PrintErr::from_message_error("could not copy binary file", Box::from(err));
        }
    }

    println!("copied binary file");
    return Ok(());
}