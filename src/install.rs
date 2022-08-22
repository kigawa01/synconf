use std::env::current_exe;
use std::fs::copy;
use std::io::Error;
use std::path::PathBuf;

use crate::print_error;

pub fn command_install() {
    println!("install synconf...");

    match copy_exe() {
        Ok(_) => {}
        Err(_) => {
            return;
        }
    }

    println!("installed synconf");
}

// #[cfg(target_os = "linux")]
fn copy_exe() -> Result<(), Error> {
    println!("coping binary file...");
    let from_path: PathBuf;
    match current_exe() {
        Ok(path) => { from_path = path; }
        Err(err) => {
            return print_error(err, "could not get current binary file path");
        }
    }

    let to_path = PathBuf::from("/usr/local/bin/synconf");

    match copy(from_path.as_path(), to_path.as_path()) {
        Ok(_) => {}
        Err(err) => {
            return print_error(err, "could not copy binary file");
        }
    }

    println!("copied binary file");
    return Ok(());
}