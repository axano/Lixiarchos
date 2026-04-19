#!/bin/bash
# Helper script to run Ansible playbook for installing Python packages

set -e

INVENTORY_FILE="ansible/inventory/hosts.yml"
PLAYBOOK_FILE="ansible/install-python-packages.yml"
LOG_FILE="logs/ansible-python-install.log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}=== Ansible Python Packages Installation ===${NC}"

# Check if Ansible is installed
if ! command -v ansible &> /dev/null; then
    echo -e "${RED}Ansible is not installed. Please install it first:${NC}"
    echo "  pip install ansible"
    exit 1
fi

# Check if playbook file exists
if [ ! -f "$PLAYBOOK_FILE" ]; then
    echo -e "${RED}Playbook file not found: $PLAYBOOK_FILE${NC}"
    exit 1
fi

# Check if inventory file exists
if [ ! -f "$INVENTORY_FILE" ]; then
    echo -e "${RED}Inventory file not found: $INVENTORY_FILE${NC}"
    exit 1
fi

# Run Ansible playbook
echo -e "${GREEN}Running Ansible playbook...${NC}"
ansible-playbook \
    -i "$INVENTORY_FILE" \
    "$PLAYBOOK_FILE" \
    -v \
    2>&1 | tee -a "$LOG_FILE"

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Playbook executed successfully${NC}"
else
    echo -e "${RED}✗ Playbook execution failed${NC}"
    exit 1
fi

