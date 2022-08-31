use std::env::current_exe;
use std::fs::{copy, File};
use std::io::{stdin, stdout, Write};
use std::path::{Path, PathBuf};
use std::process::Command;

use crate::errors::{Error, PrintErr};

pub fn command_install() {
    println!("install synconf...");

    match copy_binary_os() {
        Ok(_) => {}
        Err(_) => {
            return;
        }
    }
    let url = match read_url() {
        Ok(str) => { str }
        Err(_) => {
            return;
        }
    };

    match git_clone(url.as_str()) {
        Ok(_) => {}
        Err(_) => {
            return;
        }
    }

    match install_java_os() {
        Ok(_) => {}
        Err(_) => {
            return;
        }
    }

    println!("installed synconf");
}

#[cfg(not(target_os = "linux"))]
fn install_java_os() -> Result<(), Error> {
    return PrintErr::from_message("non-linux is not supported");
}

#[cfg(target_os = "linux")]
fn install_java_os() -> Result<(), Error> {
    install_java_linux()
}


fn install_java_linux() -> Result<(), Error> {
    match Command::new("java").arg("-version").spawn() {
        Ok(_) => { return Ok(()); }
        Err(_) => {}
    }
    return match Command::new("apt").arg("install").arg("default-jdk-headless").spawn() {
        Ok(_) => { Ok(()) }
        Err(e) => {
            PrintErr::from_message_error("could not install java", Box::from(e))
        }
    };
}

#[cfg(not(target_os = "linux"))]
fn create_service_os() -> Result<(), Error> {
    return PrintErr::from_message("non-linux is not supported");
}

#[cfg(target_os = "linux")]
fn create_service_os() -> Result<(), Error> {
    create_service_linux()
}

fn create_service_linux() -> Result<(), Error> {
    let path = Path::new("/etc/systemd/system/synconf.service");
    if !path.is_file() {
        return PrintErr::from_message("unit file is already exist");
    }
    match File::create(path) {
        Ok(_) => {}
        Err(_) => {
            return PrintErr::from_message("could not create unit file");
        }
    }
    let mut file = match File::open(path) {
        Ok(f) => { f }
        Err(_) => {
            return PrintErr::from_message("could not open file");
        }
    };
    match file.write_all(b"[Unit]
Description=Java_app
After=network-online.target

[Service]
ExecStart=synconf start
ExecStop=synconf stop
WorkingDirectory=/var/synconf
Restart=always
User=root
Group=root
Type=forking

[Install]
WantedBy=multi-user.target") {
        Ok(_) => {}
        Err(_) => {
            return PrintErr::from_message("could not write unit file");
        }
    }
    match Command::new("systemctl").arg("enable").arg("synconf").spawn() {
        Ok(_) => {}
        Err(_) => { return PrintErr::from_message("could not enable synconf"); }
    }
    match Command::new("systemctl").arg("start").arg("synconf").spawn() {
        Ok(_) => {}
        Err(_) => { return PrintErr::from_message("could not start synconf"); }
    }

    return Ok(());
}

#[cfg(target_os = "linux")]
fn install_git_os() -> Result<(), Error> {
    return install_git_linux();
}

#[cfg(not(target_os = "linux"))]
fn install_git_os() -> Result<(), Error> {
    return PrintErr::from_message("non-linux is not supported");
}

fn install_git_linux() -> Result<(), Error> {
    let mut apt = Command::new("apt");
    apt.arg("install").arg("git");
    if let Ok(_) = apt.spawn() {
        return Ok(());
    }
    return PrintErr::from_message("could not install git");
}

fn read_url() -> Result<String, Error> {
    println!("type git url");
    let _ = stdout().flush();
    let mut str = String::new();
    return match stdin().read_line(&mut str) {
        Ok(_) => { Ok(str) }
        Err(e) => {
            PrintErr::from_message_error("could not read line", Box::from(e))
        }
    };
}

fn git_clone(url: &str) -> Result<(), Error> {
    let mut clone = Command::new("git");
    clone.arg("clone").arg(url).current_dir("/var/synconf");

    match clone.spawn() {
        Ok(_) => {}
        Err(_) => {
            match install_git_os() {
                Ok(_) => {}
                Err(e) => {
                    return PrintErr::from_message_error(
                        "could not install git",
                        Box::from(e),
                    );
                }
            }
            match clone.spawn() {
                Ok(_) => {}
                Err(e) => {
                    return PrintErr::from_message_error(
                        "cold not clone",
                        Box::from(e),
                    );
                }
            }
        }
    }
    return Ok(());
}

#[cfg(target_os = "linux")]
fn copy_binary_os() -> Result<(), Error> {
    return copy_binary("/usr/local/bin/synconf");
}

#[cfg(not(target_os = "linux"))]
fn copy_binary_os() -> Result<(), Error> {
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

    if from_path == to_path {
        println!("pass is the same");
        return Ok(());
    }

    match copy(from_path.as_path(), to_path.as_path()) {
        Ok(_) => {}
        Err(err) => {
            return PrintErr::from_message_error("could not copy binary file", Box::from(err));
        }
    }

    println!("copied binary file");
    return Ok(());
}