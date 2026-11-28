
package com.possaas.possaasbackend.purchaseorder;

import com.possaas.possaasbackend.branch.Branch;
import com.possaas.possaasbackend.branch.BranchRepository;
import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.business.BusinessRepository;
import com.possaas.possaasbackend.product.Product;
import com.possaas.possaasbackend.product.ProductRepository;
import com.possaas.possaasbackend.productvariant.ProductVariant;
import com.possaas.possaasbackend.productvariant.ProductVariantRepository;
import com.possaas.possaasbackend.purchaseorder.dto.CreatePurchaseOrderRequest;
import com.possaas.possaasbackend.purchaseorder.dto.PurchaseOrderItemRequest;
import com.possaas.possaasbackend.purchaseorder.dto.PurchaseOrderItemResponse;
import com.possaas.possaasbackend.purchaseorder.dto.PurchaseOrderResponse;
import com.possaas.possaasbackend.purchaseorder.dto.UpdatePurchaseOrderRequest;
import com.possaas.possaasbackend.supplier.Supplier;
import com.possaas.possaasbackend.supplier.SupplierRepository;
import com.possaas.possaasbackend.user.User;
import com.possaas.possaasbackend.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PurchaseOrderService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final BusinessRepository businessRepository;
    private final BranchRepository branchRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    public PurchaseOrderService(
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseOrderItemRepository purchaseOrderItemRepository,
            BusinessRepository businessRepository,
            BranchRepository branchRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository,
            ProductVariantRepository productVariantRepository,
            UserRepository userRepository
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.businessRepository = businessRepository;
        this.branchRepository = branchRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // CREATE PURCHASE ORDER
    // =========================================================

    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(
            UUID businessId,
            UUID tenantId,
            UUID createdById,
            CreatePurchaseOrderRequest request
    ) {
        Business business = businessRepository.findByIdAndTenantId(businessId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found"));

        Branch branch = branchRepository.findByIdAndBusiness_Id(request.branchId(), businessId)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found for this business"));

        Supplier supplier = supplierRepository.findByIdAndBusiness_Id(request.supplierId(), businessId)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found for this business"));

        if (!supplier.isActive()) {
            throw new IllegalArgumentException("Supplier is inactive");
        }

        User createdBy = userRepository.findByIdAndBusiness_Id(createdById, businessId)
                .orElseThrow(() -> new EntityNotFoundException("User not found for this business"));

        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setBusiness(business);
        purchaseOrder.setBranch(branch);
        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setPoNumber(generatePoNumber());
        purchaseOrder.setStatus(PurchaseOrderStatus.DRAFT);
        purchaseOrder.setOrderDate(LocalDate.now());
        purchaseOrder.setExpectedDeliveryDate(request.expectedDeliveryDate());
        purchaseOrder.setNotes(request.notes());
        purchaseOrder.setCreatedBy(createdBy);

        List<PurchaseOrderItem> items = new ArrayList<>();

        BigDecimal orderSubtotal = BigDecimal.ZERO;
        BigDecimal orderTax = BigDecimal.ZERO;

        for (PurchaseOrderItemRequest itemRequest : request.items()) {
            Product product = productRepository.findByIdAndBusiness_Id(
                            itemRequest.productId(), businessId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Product not found for this business: " + itemRequest.productId()));

            if (!product.isActive()) {
                throw new IllegalArgumentException("Product is inactive: " + product.getName());
            }

            ProductVariant variant = null;

            if (itemRequest.productVariantId() != null) {
                variant = productVariantRepository
                        .findByIdAndBusinessIdAndProductId(
                                itemRequest.productVariantId(),
                                businessId,
                                product.getId()
                        )
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Variant not found for this product and business"));

                if (!Boolean.TRUE.equals(variant.getActive())) {
                    throw new IllegalArgumentException("Product variant is inactive");
                }
            }

            validateItem(itemRequest);

            BigDecimal quantity = BigDecimal.valueOf(itemRequest.quantityOrdered());
            BigDecimal unitCost = itemRequest.unitCost();
            BigDecimal taxRate = itemRequest.taxRate() != null
                    ? itemRequest.taxRate()
                    : BigDecimal.ZERO;

            BigDecimal subtotal = unitCost.multiply(quantity)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal taxAmount = subtotal.multiply(taxRate)
                    .divide(HUNDRED, 2, RoundingMode.HALF_UP);

            BigDecimal totalAmount = subtotal.add(taxAmount)
                    .setScale(2, RoundingMode.HALF_UP);

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(purchaseOrder);
            item.setProduct(product);
            item.setProductVariant(variant);
            item.setQuantityOrdered(itemRequest.quantityOrdered());
            item.setQuantityReceived(0);
            item.setUnitCost(unitCost);
            item.setTaxRate(taxRate);
            item.setSubtotal(subtotal);
            item.setTaxAmount(taxAmount);
            item.setTotalAmount(totalAmount);

            items.add(item);

            orderSubtotal = orderSubtotal.add(subtotal);
            orderTax = orderTax.add(taxAmount);
        }

        orderSubtotal = orderSubtotal.setScale(2, RoundingMode.HALF_UP);
        orderTax = orderTax.setScale(2, RoundingMode.HALF_UP);

        BigDecimal orderTotal = orderSubtotal.add(orderTax)
                .setScale(2, RoundingMode.HALF_UP);

        purchaseOrder.setSubtotal(orderSubtotal);
        purchaseOrder.setTaxAmount(orderTax);
        purchaseOrder.setTotalAmount(orderTotal);

        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        List<PurchaseOrderItem> savedItems = purchaseOrderItemRepository.saveAll(items);

        return toResponse(savedPurchaseOrder, savedItems);
    }

    // =========================================================
    // SUBMIT PURCHASE ORDER
    // =========================================================

    @Transactional
    public PurchaseOrderResponse submitPurchaseOrder(
            UUID businessId,
            UUID tenantId,
            UUID purchaseOrderId
    ) {
        businessRepository.findByIdAndTenantId(businessId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found"));

        PurchaseOrder purchaseOrder = purchaseOrderRepository
                .findByIdAndBusinessId(purchaseOrderId, businessId)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found"));

        if (purchaseOrder.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only draft purchase orders can be submitted"
            );
        }

        List<PurchaseOrderItem> items =
                purchaseOrderItemRepository.findByPurchaseOrderIdOrderByIdAsc(
                        purchaseOrderId
                );

        if (items.isEmpty()) {
            throw new IllegalStateException(
                    "Purchase order must contain at least one item before submission"
            );
        }

        if (!purchaseOrder.getSupplier().isActive()) {
            throw new IllegalStateException("Cannot submit purchase order for an inactive supplier");
        }

        if (!purchaseOrder.getBranch().isActive()) {
            throw new IllegalStateException("Cannot submit purchase order for an inactive branch");
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.SUBMITTED);

        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        return toResponse(savedPurchaseOrder, items);
    }

    // =========================================================
    // APPROVE PURCHASE ORDER
    // =========================================================

    @Transactional
    public PurchaseOrderResponse approvePurchaseOrder(
            UUID businessId,
            UUID tenantId,
            UUID purchaseOrderId,
            UUID approverId
    ) {
        businessRepository.findByIdAndTenantId(businessId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found"));

        PurchaseOrder purchaseOrder = purchaseOrderRepository
                .findByIdAndBusinessId(purchaseOrderId, businessId)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found"));

        if (purchaseOrder.getStatus() != PurchaseOrderStatus.SUBMITTED) {
            throw new IllegalStateException(
                    "Only submitted purchase orders can be approved"
            );
        }

        User approver = userRepository.findByIdAndBusiness_Id(approverId, businessId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Approver not found for this business"
                ));

        if (!purchaseOrder.getSupplier().isActive()) {
            throw new IllegalStateException(
                    "Cannot approve purchase order for an inactive supplier"
            );
        }

        List<PurchaseOrderItem> items =
                purchaseOrderItemRepository.findByPurchaseOrderIdOrderByIdAsc(
                        purchaseOrderId
                );

        if (items.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot approve a purchase order without items"
            );
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.APPROVED);
        purchaseOrder.setApprovedBy(approver);
        purchaseOrder.setApprovedAt(LocalDateTime.now());

        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        return toResponse(savedPurchaseOrder, items);
    }

    // =========================================================
    // REJECT PURCHASE ORDER
    // =========================================================

    @Transactional
    public PurchaseOrderResponse rejectPurchaseOrder(
            UUID businessId,
            UUID tenantId,
            UUID purchaseOrderId,
            UUID rejectedById
    ) {
        businessRepository.findByIdAndTenantId(businessId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found"));

        PurchaseOrder purchaseOrder = purchaseOrderRepository
                .findByIdAndBusinessId(purchaseOrderId, businessId)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found"));

        if (purchaseOrder.getStatus() != PurchaseOrderStatus.SUBMITTED) {
            throw new IllegalStateException(
                    "Only submitted purchase orders can be rejected"
            );
        }

        userRepository.findByIdAndBusiness_Id(rejectedById, businessId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Rejecting user not found for this business"
                ));

        List<PurchaseOrderItem> items =
                purchaseOrderItemRepository.findByPurchaseOrderIdOrderByIdAsc(
                        purchaseOrderId
                );

        if (items.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot reject a purchase order without items"
            );
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.REJECTED);

        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        return toResponse(savedPurchaseOrder, items);
    }



    // =========================================================
// CANCEL PURCHASE ORDER
// =========================================================

    @Transactional
    public PurchaseOrderResponse cancelPurchaseOrder(
            UUID businessId,
            UUID tenantId,
            UUID purchaseOrderId
    ) {
        businessRepository.findByIdAndTenantId(businessId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found"));

        PurchaseOrder purchaseOrder = purchaseOrderRepository
                .findByIdAndBusinessId(purchaseOrderId, businessId)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found"));

        PurchaseOrderStatus currentStatus = purchaseOrder.getStatus();

        if (currentStatus != PurchaseOrderStatus.DRAFT
                && currentStatus != PurchaseOrderStatus.SUBMITTED
                && currentStatus != PurchaseOrderStatus.APPROVED) {
            throw new IllegalStateException(
                    "Purchase order cannot be cancelled in its current status: " + currentStatus
            );
        }

        List<PurchaseOrderItem> items =
                purchaseOrderItemRepository.findByPurchaseOrderIdOrderByIdAsc(
                        purchaseOrderId
                );

        if (items.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot cancel a purchase order without items"
            );
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.CANCELLED);

        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        return toResponse(savedPurchaseOrder, items);
    }

    // =========================================================
    // GET PURCHASE ORDERS
    // =========================================================

    @Transactional(readOnly = true)
    public Page<PurchaseOrderResponse> getPurchaseOrders(
            UUID businessId,
            UUID tenantId,
            PurchaseOrderStatus status,
            Pageable pageable
    ) {
        businessRepository.findByIdAndTenantId(businessId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found"));

        Page<PurchaseOrder> purchaseOrders;

        if (status != null) {
            purchaseOrders = purchaseOrderRepository.findByBusinessIdAndStatus(
                    businessId,
                    status,
                    pageable
            );
        } else {
            purchaseOrders = purchaseOrderRepository.findByBusinessId(
                    businessId,
                    pageable
            );
        }

        return purchaseOrders.map(purchaseOrder -> {
            List<PurchaseOrderItem> items =
                    purchaseOrderItemRepository.findByPurchaseOrderIdOrderByIdAsc(
                            purchaseOrder.getId()
                    );

            return toResponse(purchaseOrder, items);
        });
    }

    // =========================================================
    // GET PURCHASE ORDER BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPurchaseOrderById(
            UUID businessId,
            UUID tenantId,
            UUID purchaseOrderId
    ) {
        businessRepository.findByIdAndTenantId(businessId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found"));

        PurchaseOrder purchaseOrder = purchaseOrderRepository
                .findByIdAndBusinessId(purchaseOrderId, businessId)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found"));

        List<PurchaseOrderItem> items =
                purchaseOrderItemRepository.findByPurchaseOrderIdOrderByIdAsc(
                        purchaseOrder.getId()
                );

        return toResponse(purchaseOrder, items);
    }

    // =========================================================
    // UPDATE PURCHASE ORDER (DRAFT ONLY)
    // =========================================================

    @Transactional
    public PurchaseOrderResponse updatePurchaseOrder(
            UUID businessId,
            UUID tenantId,
            UUID purchaseOrderId,
            UpdatePurchaseOrderRequest request
    ) {
        Business business = businessRepository.findByIdAndTenantId(businessId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found"));

        PurchaseOrder purchaseOrder = purchaseOrderRepository
                .findByIdAndBusinessId(purchaseOrderId, businessId)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found"));

        if (purchaseOrder.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new IllegalStateException("Only draft purchase orders can be updated");
        }

        Branch branch = branchRepository.findByIdAndBusiness_Id(request.branchId(), businessId)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found for this business"));

        Supplier supplier = supplierRepository.findByIdAndBusiness_Id(request.supplierId(), businessId)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found for this business"));

        if (!supplier.isActive()) {
            throw new IllegalArgumentException("Supplier is inactive");
        }

        List<PurchaseOrderItem> updatedItems = new ArrayList<>();

        BigDecimal orderSubtotal = BigDecimal.ZERO;
        BigDecimal orderTax = BigDecimal.ZERO;

        for (PurchaseOrderItemRequest itemRequest : request.items()) {
            validateItem(itemRequest);

            Product product = productRepository.findByIdAndBusiness_Id(
                            itemRequest.productId(), businessId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Product not found for this business: " + itemRequest.productId()));

            if (!product.isActive()) {
                throw new IllegalArgumentException("Product is inactive: " + product.getName());
            }

            ProductVariant variant = null;

            if (itemRequest.productVariantId() != null) {
                variant = productVariantRepository
                        .findByIdAndBusinessIdAndProductId(
                                itemRequest.productVariantId(),
                                businessId,
                                product.getId()
                        )
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Variant not found for this product and business"));

                if (!Boolean.TRUE.equals(variant.getActive())) {
                    throw new IllegalArgumentException("Product variant is inactive");
                }
            }

            BigDecimal quantity = BigDecimal.valueOf(itemRequest.quantityOrdered());
            BigDecimal unitCost = itemRequest.unitCost();
            BigDecimal taxRate = itemRequest.taxRate() != null
                    ? itemRequest.taxRate()
                    : BigDecimal.ZERO;

            BigDecimal subtotal = unitCost.multiply(quantity)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal taxAmount = subtotal.multiply(taxRate)
                    .divide(HUNDRED, 2, RoundingMode.HALF_UP);

            BigDecimal totalAmount = subtotal.add(taxAmount)
                    .setScale(2, RoundingMode.HALF_UP);

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(purchaseOrder);
            item.setProduct(product);
            item.setProductVariant(variant);
            item.setQuantityOrdered(itemRequest.quantityOrdered());
            item.setQuantityReceived(0);
            item.setUnitCost(unitCost);
            item.setTaxRate(taxRate);
            item.setSubtotal(subtotal);
            item.setTaxAmount(taxAmount);
            item.setTotalAmount(totalAmount);

            updatedItems.add(item);

            orderSubtotal = orderSubtotal.add(subtotal);
            orderTax = orderTax.add(taxAmount);
        }

        orderSubtotal = orderSubtotal.setScale(2, RoundingMode.HALF_UP);
        orderTax = orderTax.setScale(2, RoundingMode.HALF_UP);

        BigDecimal orderTotal = orderSubtotal.add(orderTax)
                .setScale(2, RoundingMode.HALF_UP);

        purchaseOrder.setBranch(branch);
        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setExpectedDeliveryDate(request.expectedDeliveryDate());
        purchaseOrder.setNotes(request.notes());
        purchaseOrder.setSubtotal(orderSubtotal);
        purchaseOrder.setTaxAmount(orderTax);
        purchaseOrder.setTotalAmount(orderTotal);

        purchaseOrderRepository.save(purchaseOrder);

        purchaseOrderItemRepository.deleteByPurchaseOrderId(purchaseOrderId);

        List<PurchaseOrderItem> savedItems =
                purchaseOrderItemRepository.saveAll(updatedItems);

        return toResponse(purchaseOrder, savedItems);
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateItem(PurchaseOrderItemRequest itemRequest) {
        if (itemRequest.quantityOrdered() == null || itemRequest.quantityOrdered() <= 0) {
            throw new IllegalArgumentException("Quantity ordered must be greater than zero");
        }

        if (itemRequest.unitCost() == null
                || itemRequest.unitCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit cost cannot be negative");
        }

        if (itemRequest.taxRate() != null
                && itemRequest.taxRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tax rate cannot be negative");
        }
    }

    // =========================================================
    // PO NUMBER GENERATION
    // =========================================================

    private String generatePoNumber() {
        return "PO-" + LocalDate.now().getYear() + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // =========================================================
    // RESPONSE MAPPING
    // =========================================================

    private PurchaseOrderResponse toResponse(
            PurchaseOrder purchaseOrder,
            List<PurchaseOrderItem> items
    ) {
        List<PurchaseOrderItemResponse> itemResponses = items.stream()
                .map(item -> new PurchaseOrderItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProductVariant() != null
                                ? item.getProductVariant().getId()
                                : null,
                        item.getQuantityOrdered(),
                        item.getQuantityReceived(),
                        item.getUnitCost(),
                        item.getTaxRate(),
                        item.getSubtotal(),
                        item.getTaxAmount(),
                        item.getTotalAmount()
                ))
                .toList();

        return new PurchaseOrderResponse(
                purchaseOrder.getId(),
                purchaseOrder.getBusiness().getId(),
                purchaseOrder.getBranch().getId(),
                purchaseOrder.getSupplier().getId(),
                purchaseOrder.getPoNumber(),
                purchaseOrder.getStatus(),
                purchaseOrder.getOrderDate(),
                purchaseOrder.getExpectedDeliveryDate(),
                purchaseOrder.getSubtotal(),
                purchaseOrder.getTaxAmount(),
                purchaseOrder.getTotalAmount(),
                purchaseOrder.getNotes(),
                purchaseOrder.getCreatedBy() != null
                        ? purchaseOrder.getCreatedBy().getId()
                        : null,
                purchaseOrder.getApprovedBy() != null
                        ? purchaseOrder.getApprovedBy().getId()
                        : null,
                purchaseOrder.getApprovedAt(),
                purchaseOrder.getCreatedAt(),
                purchaseOrder.getUpdatedAt(),
                itemResponses
        );
    }
}
