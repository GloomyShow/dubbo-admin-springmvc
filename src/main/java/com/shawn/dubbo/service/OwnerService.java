package com.shawn.dubbo.service;



import com.shawn.dubbo.dao.Owner;

import java.util.List;

public interface OwnerService {
	
	List<String> findAllServiceNames();

	List<String> findServiceNamesByUsername(String username);

	List<String> findUsernamesByServiceName(String serviceName);
	
	List<Owner> findByService(String serviceName);
	
	List<Owner> findAll();
	
	Owner findById(Long id);
	
	void saveOwner(Owner owner);
	
	void deleteOwner(Owner owner);
	
}
