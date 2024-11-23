package com.ilmatty98;

import com.ilmatty98.entity.User;
import com.ilmatty98.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.jboss.resteasy.reactive.RestPath;

@Path("/user")
@RequiredArgsConstructor
public class UpdateResource {


    private final UserRepository userRepository;

    @GET
    @Path("/{id}")
    public User getUser(@RestPath Long id) {
        return userRepository.findById(id);
    }

    @POST
    @Transactional
    public User saveUser(@RequestBody User user) {
        user.setId(null);
        userRepository.persist(user);
        return user;
    }

    @POST
    @Path("/{id}")
    @Transactional
    public void deleteUserById(@RestPath Long id) {
        userRepository.deleteById(id);
    }
}
