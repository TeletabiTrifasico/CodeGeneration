<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue';

// Define the filter structure
interface UserFilters {
  name: string;
  username: string;
  email: string;
  role: string;
  status: string;
  minAccounts: number | null;
  minBalance: number | null;
  maxBalance: number | null;
}

const props = defineProps({
  initialFilters: {
    type: Object as () => Partial<UserFilters>,
    default: () => ({})
  }
});

// Make sure the emit is working properly
const emit = defineEmits(['filters-changed']);

// Local reactive state
const isExpanded = ref(false);
const localFilters = reactive<UserFilters>({
  name: '',
  username: '',
  email: '',
  role: '',
  status: '',
  minAccounts: null,
  minBalance: null,
  maxBalance: null
});

// Computed properties
const hasActiveFilters = computed(() => {
  return Object.values(localFilters).some(value => 
    value !== null && value !== '' && value !== undefined
  );
});

// Initialize filters from props
onMounted(() => {
  if (props.initialFilters) {
    Object.keys(props.initialFilters).forEach(key => {
      if (key in localFilters && props.initialFilters[key] !== undefined) {
        localFilters[key] = props.initialFilters[key];
      }
    });
  }
});

// Methods
const toggleFilter = () => {
  isExpanded.value = !isExpanded.value;
};

// Debug the filter emission
const applyFilters = () => {
  const filterObj = { ...localFilters };
  console.log('UserFilter: Applying filters:', filterObj);
  console.log('UserFilter: Filter serialized:', 
    new URLSearchParams(
      Object.entries(filterObj)
        .filter(([_, val]) => val !== null && val !== '' && val !== undefined)
        .map(([key, val]) => [key, val.toString()])
    ).toString()
  );
  emit('filters-changed', filterObj);
};

const clearAllFilters = () => {
  Object.keys(localFilters).forEach(key => {
    localFilters[key] = key.startsWith('min') || key.startsWith('max') ? null : '';
  });
  emit('filters-changed', { ...localFilters });
};

const clearFilter = (filterName: keyof UserFilters) => {
  localFilters[filterName] = (filterName.startsWith('min') || filterName.startsWith('max')) ? null : '';
  emit('filters-changed', { ...localFilters });
};

const formatCurrency = (amount: number): string => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'EUR',
  }).format(amount);
};

// Add debug logging to watch
watch(localFilters, (newFilters) => {
  console.log('UserFilter: Local filters changed:', newFilters);
  applyFilters();
}, { deep: true });
</script>

<template>
  <div class="user-filter">
    <div class="filter-header" @click="toggleFilter">
      <h3>Filter Users</h3>
      <div class="filter-actions">
        <button 
          v-if="hasActiveFilters" 
          class="clear-button" 
          @click.stop="clearAllFilters"
          title="Clear all filters"
        >
          Clear All
        </button>
        <button class="toggle-button" @click.stop="toggleFilter">
          {{ isExpanded ? 'Hide Filters' : 'Show Filters' }}
        </button>
      </div>
    </div>

    <div v-if="isExpanded" class="filter-content">
      <!-- Name Filter -->
      <div class="filter-group">
        <h4>User Name</h4>
        <div class="input-group">
          <input
            type="text"
            v-model="localFilters.name"
            placeholder="Search by name..."
            class="name-input"
          />
        </div>
      </div>

      <!-- Username Filter -->
      <div class="filter-group">
        <h4>Username</h4>
        <div class="input-group">
          <input
            type="text"
            v-model="localFilters.username"
            placeholder="Search by username..."
            class="username-input"
          />
        </div>
      </div>

      <!-- Email Filter -->
      <div class="filter-group">
        <h4>Email</h4>
        <div class="input-group">
          <input
            type="text"
            v-model="localFilters.email"
            placeholder="Search by email..."
            class="email-input"
          />
        </div>
      </div>

      <!-- Role Filter -->
      <div class="filter-group">
        <h4>Role</h4>
        <select v-model="localFilters.role" class="select-input">
          <option value="">All Roles</option>
          <option value="CUSTOMER">Customer</option>
          <option value="EMPLOYEE">Employee</option>
        </select>
      </div>

      <!-- Status Filter -->
      <div class="filter-group">
        <h4>Status</h4>
        <select v-model="localFilters.status" class="select-input">
          <option value="">All Statuses</option>
          <option value="true">Active</option>
          <option value="false">Disabled</option>
        </select>
      </div>

      <!-- Account Count Filter -->
      <div class="filter-group">
        <h4>Minimum Accounts</h4>
        <div class="input-group">
          <input
            type="number"
            v-model.number="localFilters.minAccounts"
            placeholder="Min accounts..."
            class="number-input"
            min="0"
          />
        </div>
      </div>

      <!-- Balance Range Filter -->
      <div class="filter-group">
        <h4>Balance Range</h4>
        <div class="input-group range-group">
          <input
            type="number"
            v-model.number="localFilters.minBalance"
            placeholder="Min balance..."
            class="number-input"
            step="100"
          />
          <span class="range-separator">to</span>
          <input
            type="number"
            v-model.number="localFilters.maxBalance"
            placeholder="Max balance..."
            class="number-input"
            step="100"
          />
        </div>
      </div>

      <!-- Active Filters Display -->
      <div v-if="hasActiveFilters" class="active-filters">
        <h4>Active Filters:</h4>
        <div class="filter-tags">
          <span v-if="localFilters.name" class="filter-tag">
            Name: {{ localFilters.name }}
            <button @click="clearFilter('name')" class="remove-tag">×</button>
          </span>
          <span v-if="localFilters.username" class="filter-tag">
            Username: {{ localFilters.username }}
            <button @click="clearFilter('username')" class="remove-tag">×</button>
          </span>
          <span v-if="localFilters.email" class="filter-tag">
            Email: {{ localFilters.email }}
            <button @click="clearFilter('email')" class="remove-tag">×</button>
          </span>
          <span v-if="localFilters.role" class="filter-tag">
            Role: {{ localFilters.role }}
            <button @click="clearFilter('role')" class="remove-tag">×</button>
          </span>
          <span v-if="localFilters.status" class="filter-tag">
            Status: {{ localFilters.status === 'true' ? 'Active' : 'Disabled' }}
            <button @click="clearFilter('status')" class="remove-tag">×</button>
          </span>
          <span v-if="localFilters.minAccounts" class="filter-tag">
            Min Accounts: {{ localFilters.minAccounts }}
            <button @click="clearFilter('minAccounts')" class="remove-tag">×</button>
          </span>
          <span v-if="localFilters.minBalance" class="filter-tag">
            Min Balance: {{ formatCurrency(localFilters.minBalance) }}
            <button @click="clearFilter('minBalance')" class="remove-tag">×</button>
          </span>
          <span v-if="localFilters.maxBalance" class="filter-tag">
            Max Balance: {{ formatCurrency(localFilters.maxBalance) }}
            <button @click="clearFilter('maxBalance')" class="remove-tag">×</button>
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.user-filter {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
  overflow: hidden;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 25px;
  border-bottom: 1px solid #f0f0f0;
  background-color: #fafafa;
  cursor: pointer;
}

.filter-header h3 {
  margin: 0;
  color: #333;
  font-size: 1.2rem;
  font-weight: 600;
}

.filter-actions {
  display: flex;
  gap: 10px;
}

.clear-button, .toggle-button {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid #ddd;
}

.clear-button {
  background-color: #f44336;
  color: white;
  border-color: #f44336;
}

.clear-button:hover:not(:disabled) {
  background-color: #d32f2f;
}

.clear-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.toggle-button {
  background-color: white;
  color: #333;
}

.toggle-button:hover {
  background-color: #f5f5f5;
}

.filter-content {
  padding: 25px;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.filter-group {
  margin-bottom: 20px;
}

.filter-group h4 {
  margin: 0 0 10px 0;
  font-size: 1rem;
  color: #555;
}

.input-group {
  display: flex;
  gap: 10px;
}

.range-group {
  align-items: center;
}

.range-separator {
  color: #666;
  font-weight: 500;
}

input[type="text"],
input[type="number"],
.select-input {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9rem;
  width: 100%;
  transition: border-color 0.2s ease;
}

input[type="text"]:focus,
input[type="number"]:focus,
.select-input:focus {
  border-color: #4CAF50;
  outline: none;
}

.name-input,
.username-input,
.email-input {
  width: 100%;
}

.number-input {
  width: 100%;
}

.active-filters {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.active-filters h4 {
  margin-bottom: 12px;
  color: #4CAF50;
}

.filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-tag {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background-color: #e8f5e8;
  color: #2e7d32;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
}

.remove-tag {
  background: none;
  border: none;
  color: #2e7d32;
  cursor: pointer;
  font-size: 1.2rem;
  line-height: 1;
  padding: 0;
}

.remove-tag:hover {
  color: #1b5e20;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .filter-content {
    padding: 20px 15px;
  }
  
  .range-group {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .range-separator {
    margin: 5px 0;
  }
}
</style>