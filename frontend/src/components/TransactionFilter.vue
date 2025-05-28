<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue';
import { TransactionFilters } from '@/services/api.config.ts';

interface Props {
  initialFilters?: TransactionFilters;
}

interface Emits {
  (e: 'filtersChanged', filters: TransactionFilters): void;
}

const props = withDefaults(defineProps<Props>(), {
  initialFilters: () => ({})
});

const emit = defineEmits<Emits>();

// Reactive state
const isExpanded = ref(false);
const localFilters = ref<TransactionFilters>({ ...props.initialFilters });
const activeAmountFilter = ref<'lessThan' | 'greaterThan' | 'equalTo' | ''>('');
const amountFilterValue = ref<number | null>(null);

// Computed properties
const today = computed(() => {
  return new Date().toISOString().split('T')[0];
});

const hasActiveFilters = computed(() => {
  return Object.values(localFilters.value).some(value =>
      value !== undefined && value !== null && value !== ''
  ) || (amountFilterValue.value !== null && activeAmountFilter.value);
});

const amountFilterTabs = [
  { key: 'lessThan', label: 'Less Than' },
  { key: 'greaterThan', label: 'Greater Than' },
  { key: 'equalTo', label: 'Equal To' }
];

// Methods
const toggleExpanded = () => {
  isExpanded.value = !isExpanded.value;
};

const setAmountFilterType = (type: 'lessThan' | 'greaterThan' | 'equalTo') => {
  if (activeAmountFilter.value === type) {
    activeAmountFilter.value = '';
    amountFilterValue.value = null;
    clearAmountFilters();
  } else {
    activeAmountFilter.value = type;
    updateAmountFilter();
  }
};

const updateAmountFilter = () => {
  // Clear all amount filters first
  clearAmountFilters();

  if (amountFilterValue.value !== null && activeAmountFilter.value) {
    switch (activeAmountFilter.value) {
      case 'lessThan':
        localFilters.value.amountLessThan = amountFilterValue.value;
        break;
      case 'greaterThan':
        localFilters.value.amountGreaterThan = amountFilterValue.value;
        break;
      case 'equalTo':
        localFilters.value.amountEqualTo = amountFilterValue.value;
        break;
    }
  }

  applyFilters();
};

const clearAmountFilters = () => {
  delete localFilters.value.amountLessThan;
  delete localFilters.value.amountGreaterThan;
  delete localFilters.value.amountEqualTo;
};

const clearAmountFilter = () => {
  activeAmountFilter.value = '';
  amountFilterValue.value = null;
  clearAmountFilters();
  applyFilters();
};

const getAmountPlaceholder = () => {
  switch (activeAmountFilter.value) {
    case 'lessThan':
      return 'Amount less than...';
    case 'greaterThan':
      return 'Amount greater than...';
    case 'equalTo':
      return 'Exact amount...';
    default:
      return 'Enter amount...';
  }
};

const getAmountFilterDisplay = () => {
  const value = amountFilterValue.value;
  switch (activeAmountFilter.value) {
    case 'lessThan':
      return `Amount < ${value}`;
    case 'greaterThan':
      return `Amount > ${value}`;
    case 'equalTo':
      return `Amount = ${value}`;
    default:
      return '';
  }
};

const clearFilter = (filterKey: keyof TransactionFilters) => {
  delete localFilters.value[filterKey];
  applyFilters();
};

const clearAllFilters = () => {
  localFilters.value = {};
  activeAmountFilter.value = '';
  amountFilterValue.value = null;
  applyFilters();
};

const applyFilters = () => {
  nextTick(() => {
    emit('filtersChanged', { ...localFilters.value });
  });
};

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString();
};

// Watch for prop changes
watch(() => props.initialFilters, (newFilters) => {
  localFilters.value = { ...newFilters };

  // Set amount filter state based on props
  if (newFilters.amountLessThan) {
    activeAmountFilter.value = 'lessThan';
    amountFilterValue.value = newFilters.amountLessThan;
  } else if (newFilters.amountGreaterThan) {
    activeAmountFilter.value = 'greaterThan';
    amountFilterValue.value = newFilters.amountGreaterThan;
  } else if (newFilters.amountEqualTo) {
    activeAmountFilter.value = 'equalTo';
    amountFilterValue.value = newFilters.amountEqualTo;
  }
}, { immediate: true });
</script>
<template>
  <div class="transaction-filter">
    <div class="filter-header">
      <h3>Filter Transactions</h3>
      <div class="filter-actions">
        <button @click="clearAllFilters" class="clear-button" :disabled="!hasActiveFilters">
          Clear All
        </button>
        <button @click="toggleExpanded" class="toggle-button">
          {{ isExpanded ? 'Hide Filters' : 'Show Filters' }}
        </button>
      </div>
    </div>

    <div v-show="isExpanded" class="filter-content">
      <!-- Date Range Filter -->
      <div class="filter-group">
        <h4>Date Range</h4>
        <div class="date-inputs">
          <div class="input-group">
            <label for="startDate">From:</label>
            <input
                id="startDate"
                type="date"
                v-model="localFilters.startDate"
                :max="localFilters.endDate || today"
                @change="applyFilters"
            />
          </div>
          <div class="input-group">
            <label for="endDate">To:</label>
            <input
                id="endDate"
                type="date"
                v-model="localFilters.endDate"
                :min="localFilters.startDate"
                :max="today"
                @change="applyFilters"
            />
          </div>
        </div>
      </div>

      <!-- Amount Filter -->
      <div class="filter-group">
        <h4>Amount</h4>
        <div class="amount-filter-tabs">
          <button
              v-for="tab in amountFilterTabs"
              :key="tab.key"
              @click="setAmountFilterType(tab.key)"
              :class="{ active: activeAmountFilter === tab.key }"
              class="tab-button"
          >
            {{ tab.label }}
          </button>
        </div>
        <div class="amount-input" v-show="activeAmountFilter">
          <input
              type="number"
              step="0.01"
              min="0"
              v-model="amountFilterValue"
              @input="updateAmountFilter"
              :placeholder="getAmountPlaceholder()"
          />
        </div>
      </div>

      <!-- IBAN Filter -->
      <div class="filter-group">
        <h4>Account (IBAN)</h4>
        <div class="input-group">
          <input
              type="text"
              v-model="localFilters.iban"
              @input="applyFilters"
              placeholder="Search by account number..."
              class="iban-input"
          />
        </div>
      </div>

      <!-- Transaction Type Filter -->
      <div class="filter-group">
        <h4>Transaction Type</h4>
        <select v-model="localFilters.transactionType" @change="applyFilters" class="select-input">
          <option value="">All Types</option>
          <option value="TRANSFER">Transfer</option>
          <option value="DEPOSIT">Deposit</option>
          <option value="WITHDRAWAL">Withdrawal</option>
        </select>
      </div>

      <!-- Transaction Status Filter -->
      <div class="filter-group">
        <h4>Status</h4>
        <select v-model="localFilters.transactionStatus" @change="applyFilters" class="select-input">
          <option value="">All Statuses</option>
          <option value="PENDING">Pending</option>
          <option value="COMPLETED">Completed</option>
          <option value="FAILED">Failed</option>
          <option value="CANCELLED">Cancelled</option>
        </select>
      </div>

      <!-- Description Filter -->
      <div class="filter-group">
        <h4>Description</h4>
        <div class="input-group">
          <input
              type="text"
              v-model="localFilters.description"
              @input="applyFilters"
              placeholder="Search in description..."
              class="description-input"
          />
        </div>
      </div>

      <!-- Active Filters Display -->
      <div v-if="hasActiveFilters" class="active-filters">
        <h4>Active Filters:</h4>
        <div class="filter-tags">
          <span v-if="localFilters.startDate" class="filter-tag">
            From: {{ formatDate(localFilters.startDate) }}
            <button @click="clearFilter('startDate')" class="remove-tag">×</button>
          </span>
          <span v-if="localFilters.endDate" class="filter-tag">
            To: {{ formatDate(localFilters.endDate) }}
            <button @click="clearFilter('endDate')" class="remove-tag">×</button>
          </span>
          <span v-if="amountFilterValue && activeAmountFilter" class="filter-tag">
            {{ getAmountFilterDisplay() }}
            <button @click="clearAmountFilter" class="remove-tag">×</button>
          </span>
          <span v-if="localFilters.iban" class="filter-tag">
            IBAN: {{ localFilters.iban }}
            <button @click="clearFilter('iban')" class="remove-tag">×</button>
          </span>
          <span v-if="localFilters.transactionType" class="filter-tag">
            Type: {{ localFilters.transactionType }}
            <button @click="clearFilter('transactionType')" class="remove-tag">×</button>
          </span>
          <span v-if="localFilters.transactionStatus" class="filter-tag">
            Status: {{ localFilters.transactionStatus }}
            <button @click="clearFilter('transactionStatus')" class="remove-tag">×</button>
          </span>
          <span v-if="localFilters.description" class="filter-tag">
            Description: {{ localFilters.description }}
            <button @click="clearFilter('description')" class="remove-tag">×</button>
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.transaction-filter {
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
    max-height: 0;
  }
  to {
    opacity: 1;
    max-height: 1000px;
  }
}

.filter-group {
  margin-bottom: 25px;
}

.filter-group h4 {
  margin: 0 0 12px 0;
  color: #555;
  font-size: 1rem;
  font-weight: 600;
}

.date-inputs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.input-group label {
  font-size: 0.9rem;
  color: #666;
  font-weight: 500;
}

input[type="date"],
input[type="number"],
input[type="text"],
.select-input {
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.2s ease;
  background-color: white;
}

input:focus,
.select-input:focus {
  border-color: #4CAF50;
  outline: none;
  box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.1);
}

.amount-filter-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.tab-button {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background-color: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.9rem;
}

.tab-button:hover {
  background-color: #f5f5f5;
}

.tab-button.active {
  background-color: #4CAF50;
  color: white;
  border-color: #4CAF50;
}

.amount-input {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.iban-input,
.description-input {
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
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s ease;
}

.remove-tag:hover {
  background-color: rgba(46, 125, 50, 0.1);
}

/* Responsive Design */
@media (max-width: 768px) {
  .filter-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }

  .filter-actions {
    width: 100%;
    justify-content: space-between;
  }

  .date-inputs {
    grid-template-columns: 1fr;
  }

  .amount-filter-tabs {
    flex-direction: column;
  }

  .filter-tags {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 480px) {
  .filter-content {
    padding: 20px;
  }

  .filter-header {
    padding: 15px 20px;
  }
}
</style>