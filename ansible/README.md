# Ansible Python Packages Installation Guide

## Overview
This Ansible setup automates the installation of Python packages on Linux servers (Ubuntu, Debian, CentOS, RedHat).

## Project Structure
```
ansible/
├── inventory/
│   └── hosts.yml              # Define target servers
├── files/
│   └── python-requirements.txt # Python packages to install
└── install-python-packages.yml # Main playbook
```

## Prerequisites

1. **Ansible Installation** (on your control machine):
   ```bash
   pip install ansible
   ```

2. **SSH Access** to target Linux servers with sudo privileges

3. **Python 3** available on target servers (will be installed if not present)

## Configuration Steps

### 1. Edit Inventory File (`ansible/inventory/hosts.yml`)

Update the host IPs and credentials:
```yaml
linux_servers:
  hosts:
    dev_server:
      ansible_host: YOUR_DEV_IP
      ansible_user: your_username
```

### 2. Customize Python Requirements (`ansible/files/python-requirements.txt`)

Add/remove packages as needed for your Lixiarchos application:
```
requests==2.31.0
pandas==2.0.3
Flask==2.3.3
```

### 3. SSH Key Authentication (Optional)

For key-based authentication:
```yaml
ansible_ssh_private_key_file: ~/.ssh/id_rsa
```

## Usage

### Run the Playbook (from project root):

```bash
# Basic run
ansible-playbook -i ansible/inventory/hosts.yml ansible/install-python-packages.yml

# With verbose output
ansible-playbook -i ansible/inventory/hosts.yml ansible/install-python-packages.yml -v

# Target specific host
ansible-playbook -i ansible/inventory/hosts.yml ansible/install-python-packages.yml --limit dev_server

# Run with different user
ansible-playbook -i ansible/inventory/hosts.yml ansible/install-python-packages.yml -u ubuntu
```

### Using the Helper Script:

```bash
# Linux/Mac
chmod +x scripts/run-ansible-python.sh
./scripts/run-ansible-python.sh

# Windows (requires Git Bash or WSL)
bash scripts/run-ansible-python.sh
```

## Playbook Features

✓ **Multi-OS Support**: Works with Debian, Ubuntu, CentOS, RedHat
✓ **Package Manager Detection**: Auto-detects apt or yum
✓ **Dependency Installation**: Installs Python 3, pip, and venv
✓ **Pip Upgrade**: Upgrades pip to latest version
✓ **Batch Installation**: Installs all packages from requirements.txt
✓ **Logging**: Outputs to logs/ansible-python-install.log
✓ **Verification**: Lists installed packages after installation

## Common Tasks

### Install specific packages only:

Create a temporary requirements file and reference it:
```bash
ansible-playbook -i ansible/inventory/hosts.yml ansible/install-python-packages.yml \
  -e "requirements_file=/tmp/custom-requirements.txt"
```

### Skip installation and just verify:

```bash
ansible-playbook -i ansible/inventory/hosts.yml ansible/install-python-packages.yml \
  --check --diff
```

### Run only on production servers:

```bash
ansible-playbook -i ansible/inventory/hosts.yml ansible/install-python-packages.yml \
  --limit prod_server
```

## Troubleshooting

### SSH Connection Issues
- Verify SSH access: `ssh -i ~/.ssh/id_rsa user@host`
- Check firewall on target servers
- Ensure port 22 is open

### Permission Denied
- Ensure user has sudo privileges
- Add to sudoers: `user ALL=(ALL) NOPASSWD:ALL`

### Package Not Found
- Check package name in `python-requirements.txt`
- Verify PyPI availability from target servers

## Best Practices

1. **Test on dev first**: Always test on development server before production
2. **Version pins**: Use specific versions in requirements.txt
3. **Review changes**: Use `--check --diff` flags to preview changes
4. **Backup**: Keep backup of current environment
5. **Logs**: Check logs/ansible-python-install.log for details

## Integration with Deployment

Add to your deployment scripts:
```bash
# In deployToTest.bat or equivalent
ansible-playbook -i ansible/inventory/hosts.yml ansible/install-python-packages.yml
```

## Next Steps

1. Update `ansible/inventory/hosts.yml` with your server details
2. Customize `ansible/files/python-requirements.txt` with needed packages
3. Test on a dev server first
4. Run on test and production environments

